package com.altair288.class_management.service;

import com.altair288.class_management.model.*;
import com.altair288.class_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import com.altair288.class_management.dto.LeaveCalendarDTO;
import com.altair288.class_management.dto.CurrentUserLeaveInfoDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.altair288.class_management.MessageCenter.service.NotificationService;
import com.altair288.class_management.MessageCenter.enums.NotificationType;
import com.altair288.class_management.MessageCenter.enums.NotificationPriority;

@Service
public class LeaveRequestService {
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private LeaveTypeConfigRepository leaveTypeConfigRepository;
    
    @Autowired
    private StudentLeaveBalanceRepository studentLeaveBalanceRepository;
    
    @Autowired
    private LeaveApprovalRepository leaveApprovalRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private LeaveTypeWorkflowRepository leaveTypeWorkflowRepository;

    // 仅按请假类型绑定流程，去除班级覆盖

    @Autowired
    private ApprovalStepRepository approvalStepRepository;

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest) {
        // 设置基本信息
        // 判断请假类型是否需要审批
        boolean requiresApproval = true;
        if (leaveRequest.getLeaveTypeId() != null) {
            var cfg = leaveTypeConfigRepository.findById(leaveRequest.getLeaveTypeId()).orElse(null);
            if (cfg != null) {
                requiresApproval = Boolean.TRUE.equals(cfg.getRequiresApproval());
            }
        }
        leaveRequest.setStatus(requiresApproval ? "待审批" : "已批准");
        leaveRequest.setCreatedAt(new Date());
        leaveRequest.setUpdatedAt(new Date());
        
        // 计算请假天数
        if (leaveRequest.getStartDate() != null && leaveRequest.getEndDate() != null) {
            LocalDate start = leaveRequest.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate end = leaveRequest.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            long daysBetween = ChronoUnit.DAYS.between(start, end) + 1;
            leaveRequest.setDays((double) daysBetween);
        }
        
        // 保存请假申请
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        
        // 更新学生请假余额
        updateStudentLeaveBalance(saved);
        
        // 若需要审批才创建第一步，否则视为已完成审批
        if (requiresApproval) {
            createFirstApprovalStep(saved);
            // 通知：提交请假 -> 第一级审批人 （模板）
            try {
                var firstApprovals = leaveApprovalRepository.findPendingByLeaveId(saved.getId());
                java.util.List<Integer> approverUsers = new java.util.ArrayList<>();
                for (LeaveApproval a : firstApprovals) {
                    if (a.getTeacherId() != null) {
                        var teacherUser = userRepository.findByRelatedIdAndUserType(a.getTeacherId(), User.UserType.TEACHER);
                        teacherUser.ifPresent(u -> approverUsers.add(u.getId()));
                    }
                }
                if (!approverUsers.isEmpty()) {
                    Map<String,Object> vars = buildLeaveVariables(saved, null, null);
                    notificationService.createFromTemplate(new NotificationService.TemplateRequest(
                            NotificationType.LEAVE_SUBMITTED,
                            "LEAVE_SUBMITTED_TO_APPROVER",
                            vars,
                            NotificationPriority.NORMAL,
                            "LEAVE_REQUEST",
                            String.valueOf(saved.getId()),
                            "leave:submitted:" + saved.getId(),
                            approverUsers
                    ));
                }
            } catch (Exception ignored) {}
        } else {
            saved.setReviewedAt(new Date());
            saved = leaveRequestRepository.save(saved);
            // 通知：自动批准（模板：AUTO）
            try { notifyLeaveFinal(saved, true, true, null); } catch (Exception ignored) {}
        }
        
        return saved;
    }

    private void createFirstApprovalStep(LeaveRequest saved) {
        try {
            Integer classId = null;
            Integer departmentId = null;
            String grade = null;
            if (saved.getStudentId() != null) {
                var stuOpt = studentRepository.findById(saved.getStudentId());
                if (stuOpt.isPresent() && stuOpt.get().getClazz() != null) {
                    var clz = stuOpt.get().getClazz();
                    classId = clz.getId();
                    grade = clz.getGrade();
                    if (clz.getDepartment() != null) {
                        departmentId = clz.getDepartment().getId();
                    }
                }
            }

        // 仅按请假类型解析流程
        Integer workflowId = leaveTypeWorkflowRepository
            .findActiveByLeaveTypeId(saved.getLeaveTypeId())
            .map(w -> w.getWorkflowId())
            .orElse(null);

            if (workflowId == null) {
                return; // 未配置流程则不创建审批
            }

            var steps = approvalStepRepository.findEnabledStepsByWorkflow(workflowId);
            if (steps == null || steps.isEmpty()) return;

            var first = steps.get(0);
            // 容错：stepOrder 允许为空时设置为 1
            if (first.getStepOrder() == null) {
                first.setStepOrder(1);
            }
            String approverRoleCode = first.getApproverRole() != null ? first.getApproverRole().getCode() : null;
            Integer approverId = resolveApproverId(approverRoleCode, classId, departmentId, grade);
            if (approverId == null) return;

            LeaveApproval pending = new LeaveApproval();
            pending.setLeaveId(saved.getId());
            pending.setWorkflowId(workflowId);
            pending.setStepOrder(first.getStepOrder());
            pending.setStepName(first.getStepName());
            pending.setApproverRole(first.getApproverRole());
            pending.setTeacherId(approverId);
            pending.setStatus("待审批");
            pending.setReviewedAt(null);
            pending.setComment(null);
            pending.setCreatedAt(new Date());
            pending.setUpdatedAt(new Date());
            leaveApprovalRepository.save(pending);
        } catch (Exception ignored) {}
    }

    private Integer resolveApproverId(String role, Integer classId, Integer departmentId, String grade) {
        // 班级 > 系部 > 年级 > 全局 > 班主任兜底
        if (classId != null) {
            var c = roleAssignmentRepository.findByRoleAndClass(role, classId);
            if (c.isPresent()) return c.get().getTeacherId();
        }
        if (departmentId != null) {
            var d = roleAssignmentRepository.findByRoleAndDepartment(role, departmentId);
            if (d.isPresent()) return d.get().getTeacherId();
        }
        if (grade != null) {
            var g = roleAssignmentRepository.findByRoleAndGrade(role, grade);
            if (g.isPresent()) return g.get().getTeacherId();
        }
        var glob = roleAssignmentRepository.findGlobalByRole(role);
        if (glob.isPresent()) return glob.get().getTeacherId();

        // 兜底：如果角色为班主任，返回班主任
    if ("班主任".equals(role) && classId != null) {
        var classOpt = classId == null ? Optional.<com.altair288.class_management.model.Class>empty() : Optional.ofNullable(
            studentRepository.findAll().stream()
                .map(com.altair288.class_management.model.Student::getClazz)
                            .filter(Objects::nonNull)
                            .filter(c -> classId.equals(c.getId()))
                            .findFirst().orElse(null)
            );
            if (classOpt.isPresent() && classOpt.get().getTeacher() != null) {
                return classOpt.get().getTeacher().getId();
            }
        }
        return null;
    }

    public List<LeaveRequest> getLeaveRequestsByStudent(Integer studentId) {
        return leaveRequestRepository.findByStudentId(studentId);
    }

    public List<LeaveRequest> getLeaveRequestsByTeacher(Integer teacherId) {
    return leaveRequestRepository.findByApprover(teacherId);
    }

    public List<LeaveRequest> getLeaveRequestsByStatus(String status) {
        return leaveRequestRepository.findByStatus(status);
    }

    public List<LeaveRequest> getLeaveRequestsByDateRange(Date startDate, Date endDate) {
        return leaveRequestRepository.findByDateRange(startDate, endDate);
    }

    public List<LeaveRequest> getAll() {
        // 原方法保留语义需要时可重命名；改为返回实体仍供内部使用
        return leaveRequestRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> getAllAsDTO() {
        List<LeaveRequest> entities = leaveRequestRepository.findAll();
        return buildDTOs(entities);
    }

    // 新增：按学生/教师/状态/日期范围/单条 的 DTO 版本，避免直接暴露实体及懒加载代理
    @Transactional(readOnly = true)
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> getByStudentAsDTO(Integer studentId) {
        return buildDTOs(leaveRequestRepository.findByStudentId(studentId));
    }

    @Transactional(readOnly = true)
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> getByTeacherAsDTO(Integer teacherId) {
        return buildDTOs(leaveRequestRepository.findByApprover(teacherId));
    }

    @Transactional(readOnly = true)
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> getPendingByTeacherAsDTO(Integer teacherId) {
        return buildDTOs(leaveRequestRepository.findPendingByApprover(teacherId));
    }

    @Transactional(readOnly = true)
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> getByStatusAsDTO(String status) {
        return buildDTOs(leaveRequestRepository.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> getByDateRangeAsDTO(Date startDate, Date endDate) {
        return buildDTOs(leaveRequestRepository.findByDateRange(startDate, endDate));
    }

    @Transactional(readOnly = true)
    public com.altair288.class_management.dto.LeaveRequestListDTO getOneAsDTO(Integer id) {
        var opt = leaveRequestRepository.findById(id);
        if (opt.isEmpty()) return null;
        var list = buildDTOs(java.util.List.of(opt.get()));
        return list.isEmpty()? null : list.get(0);
    }

    // 抽取公共 DTO 构造，保证所有 GET 端点一致逻辑
    @Transactional(readOnly = true)
    protected List<com.altair288.class_management.dto.LeaveRequestListDTO> buildDTOs(List<LeaveRequest> entities) {
        if (entities == null || entities.isEmpty()) return java.util.Collections.emptyList();
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (LeaveRequest lr : entities) if (lr.getId()!=null) ids.add(lr.getId());
        if (ids.isEmpty()) return java.util.Collections.emptyList();
        var approvals = leaveApprovalRepository.findByLeaveIds(ids);
        java.util.Map<Integer, java.util.List<LeaveApproval>> grouped = new java.util.HashMap<>();
        for (LeaveApproval a: approvals) {
            java.util.List<LeaveApproval> gl = grouped.get(a.getLeaveId());
            if (gl == null) { gl = new java.util.ArrayList<>(); grouped.put(a.getLeaveId(), gl); }
            gl.add(a);
        }
        java.util.List<com.altair288.class_management.dto.LeaveRequestListDTO> list = new java.util.ArrayList<>(entities.size());
        for (LeaveRequest lr : entities) {
            com.altair288.class_management.dto.LeaveRequestListDTO dto = new com.altair288.class_management.dto.LeaveRequestListDTO();
            dto.setId(lr.getId());
            dto.setStudentId(lr.getStudentId());
            if (lr.getStudent() != null) {
                try { dto.setStudentName(lr.getStudent().getName()); } catch (Exception ignored) {}
                try { dto.setStudentNo(lr.getStudent().getStudentNo()); } catch (Exception ignored) {}
                try { if (lr.getStudent().getClazz()!=null) dto.setClassName(lr.getStudent().getClazz().getName()); } catch (Exception ignored) {}
            }
            dto.setLeaveTypeId(lr.getLeaveTypeId());
            if (lr.getLeaveTypeConfig()!=null) { try { dto.setLeaveTypeName(lr.getLeaveTypeConfig().getTypeName()); } catch (Exception ignored) {} }
            dto.setStatus(lr.getStatus());
            dto.setStartDate(lr.getStartDate());
            dto.setEndDate(lr.getEndDate());
            dto.setDays(lr.getDays());
            dto.setCreatedAt(lr.getCreatedAt());
            dto.setReviewedAt(lr.getReviewedAt());
            try { dto.setReason(lr.getReason()); } catch (Exception ignored) {}
            var apprList = grouped.getOrDefault(lr.getId(), java.util.List.of());
            apprList.sort(java.util.Comparator.comparing(LeaveApproval::getStepOrder, java.util.Comparator.nullsLast(Integer::compareTo)));
            java.util.List<com.altair288.class_management.dto.LeaveApprovalDTO> apprDTOs = new java.util.ArrayList<>(apprList.size());
            for (LeaveApproval a : apprList) {
                com.altair288.class_management.dto.LeaveApprovalDTO ad = new com.altair288.class_management.dto.LeaveApprovalDTO();
                ad.setId(a.getId());
                ad.setStepOrder(a.getStepOrder());
                ad.setStepName(a.getStepName());
                if (a.getApproverRole()!=null) {
                    ad.setRoleCode(a.getApproverRole().getCode());
                    ad.setRoleDisplayName(a.getApproverRole().getDisplayName());
                }
                ad.setTeacherId(a.getTeacherId());
                if (a.getTeacher()!=null) { try { ad.setTeacherName(a.getTeacher().getName()); } catch (Exception ignored) {} }
                ad.setComment(a.getComment());
                ad.setStatus(a.getStatus());
                ad.setReviewedAt(a.getReviewedAt());
                apprDTOs.add(ad);
            }
            dto.setApprovals(apprDTOs);
            var pending = apprList.stream().filter(a -> "待审批".equals(a.getStatus()))
                    .min(java.util.Comparator.comparing(LeaveApproval::getStepOrder, java.util.Comparator.nullsLast(Integer::compareTo)));
            if (pending.isPresent()) {
                dto.setCurrentStepName(pending.get().getStepName());
                if (pending.get().getApproverRole()!=null) {
                    dto.setPendingRoleCode(pending.get().getApproverRole().getCode());
                    dto.setPendingRoleDisplayName(pending.get().getApproverRole().getDisplayName());
                }
            } else if (!apprList.isEmpty()) {
                var last = apprList.get(apprList.size()-1);
                dto.setCurrentStepName(last.getStepName());
            }
            list.add(dto);
        }
        return list;
    }

    public LeaveRequest getById(Integer id) {
        return leaveRequestRepository.findById(id).orElse(null);
    }

    @Transactional
    public LeaveRequest approveLeaveRequest(Integer id, Integer approverId, String comments) {
        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest == null) {
            throw new RuntimeException("请假申请不存在");
        }
        // 权限校验：是否存在一条待审批记录指向该教师
    var pendingRecords = leaveApprovalRepository.findPendingByLeaveId(id);
    boolean canApprove = pendingRecords.stream().anyMatch(a -> Objects.equals(a.getTeacherId(), approverId));
    if (!canApprove && !pendingRecords.isEmpty()) {
            throw new RuntimeException("当前用户无权审批：不是此步骤指派的审批人");
        }
        // 更新当前步骤为已批准
        var exist = leaveApprovalRepository.findByLeaveIdAndTeacherId(id, approverId);
        LeaveApproval approval = exist.orElseGet(LeaveApproval::new);
        boolean isNew = (approval.getId() == null);
        // 补偿：如果当前对象没有 workflowId / stepOrder，尝试从 pendingRecords 中取（说明是第一次补建）
        if (approval.getWorkflowId() == null && !pendingRecords.isEmpty()) {
            approval.setWorkflowId(pendingRecords.get(0).getWorkflowId());
            approval.setStepOrder(pendingRecords.get(0).getStepOrder());
            approval.setStepName(pendingRecords.get(0).getStepName());
            approval.setApproverRole(pendingRecords.get(0).getApproverRole());
        }
        approval.setLeaveId(id);
        approval.setTeacherId(approverId);
        approval.setStatus("已批准");
        approval.setComment(comments);
        approval.setReviewedAt(new Date());
        approval.setUpdatedAt(new Date());
        if (isNew) {
            // 若不存在流程信息，赋默认单级审批元数据
            if (approval.getStepOrder() == null) approval.setStepOrder(1);
            if (approval.getStepName() == null) approval.setStepName("单级审批");
        }
        leaveApprovalRepository.save(approval);

        // 推进到下一步
        Integer workflowId = approval.getWorkflowId();
        Integer currentOrder = approval.getStepOrder();
        if (workflowId != null && currentOrder != null) {
            var steps = approvalStepRepository.findEnabledStepsByWorkflow(workflowId);
            if (steps != null && !steps.isEmpty()) {
                // 保证排序
                steps.sort(Comparator.comparing(ApprovalStep::getStepOrder, Comparator.nullsLast(Integer::compareTo)));
                // 查找下一个“可解析出教师”的步骤（以前只找第一个 > current 的，如果解析不到教师就卡住）
                ApprovalStep chosenNext = null;
                Integer chosenTeacherId = null;
                Integer classId = null; Integer departmentId = null; String grade = null;
                var stu = leaveRequest.getStudentId() == null ? null : studentRepository.findById(leaveRequest.getStudentId()).orElse(null);
                if (stu != null && stu.getClazz() != null) {
                    classId = stu.getClazz().getId();
                    grade = stu.getClazz().getGrade();
                    if (stu.getClazz().getDepartment() != null) departmentId = stu.getClazz().getDepartment().getId();
                }
                for (ApprovalStep s : steps) {
                    if (s.getStepOrder() != null && s.getStepOrder() > currentOrder) {
                        String roleCode = s.getApproverRole() != null ? s.getApproverRole().getCode() : null;
                        Integer tid = resolveApproverId(roleCode, classId, departmentId, grade);
                        if (tid != null) {
                            chosenNext = s; chosenTeacherId = tid; break;
                        }
                    }
                }
                if (chosenNext != null && chosenTeacherId != null) {
                    LeaveApproval pending = new LeaveApproval();
                    pending.setLeaveId(id);
                    pending.setWorkflowId(workflowId);
                    pending.setStepOrder(chosenNext.getStepOrder());
                    pending.setStepName(chosenNext.getStepName());
                    pending.setApproverRole(chosenNext.getApproverRole());
                    pending.setTeacherId(chosenTeacherId);
                    pending.setStatus("待审批");
                    pending.setCreatedAt(new Date());
                    pending.setUpdatedAt(new Date());
                    leaveApprovalRepository.save(pending);
                    // 通知：进入下一步
                    try { notifyNextStep(leaveRequest, pending); } catch (Exception ignored) {}
                } else {
                    // 没有更多可指派步骤 => 视为流程结束
                    leaveRequest.setStatus("已批准");
                    leaveRequest.setReviewedAt(new Date());
                    leaveRequest.setUpdatedAt(new Date());
                    LeaveRequest fin = leaveRequestRepository.save(leaveRequest);
                    try { notifyLeaveFinal(fin, true, false, null); } catch (Exception ignored) {}
                    return fin;
                }
            } else {
                // 没有步骤定义，兼容单级
                leaveRequest.setStatus("已批准");
                leaveRequest.setReviewedAt(new Date());
                leaveRequest.setUpdatedAt(new Date());
                LeaveRequest fin = leaveRequestRepository.save(leaveRequest);
                try { notifyLeaveFinal(fin, true, false, null); } catch (Exception ignored) {}
                return fin;
            }
        } else {
            // 无流程信息，单级审批兼容
            leaveRequest.setStatus("已批准");
            leaveRequest.setReviewedAt(new Date());
            leaveRequest.setUpdatedAt(new Date());
            LeaveRequest fin = leaveRequestRepository.save(leaveRequest);
            try { notifyLeaveFinal(fin, true, false, null); } catch (Exception ignored) {}
            return fin;
        }

        // 还有后续步骤，保持单据为待审批
        leaveRequest.setStatus("待审批");
        leaveRequest.setUpdatedAt(new Date());
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest rejectLeaveRequest(Integer id, Integer approverId, String comments) {
        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest == null) {
            throw new RuntimeException("请假申请不存在");
        }
        // 权限校验：是否存在一条待审批记录指向该教师
    var pendingRecords = leaveApprovalRepository.findPendingByLeaveId(id);
    boolean canReject = pendingRecords.stream().anyMatch(a -> Objects.equals(a.getTeacherId(), approverId));
    if (!canReject && !pendingRecords.isEmpty()) {
            throw new RuntimeException("当前用户无权审批：不是此步骤指派的审批人");
        }
        
    leaveRequest.setStatus("已拒绝");
        leaveRequest.setUpdatedAt(new Date());
        
    // 更新或创建审批记录
    var exist = leaveApprovalRepository.findByLeaveIdAndTeacherId(id, approverId);
        LeaveApproval approval = exist.orElseGet(LeaveApproval::new);
        boolean isNew = (approval.getId() == null);
    approval.setLeaveId(id);
    approval.setTeacherId(approverId);
    approval.setStatus("已拒绝");
    approval.setComment(comments);
    approval.setReviewedAt(new Date());
    approval.setUpdatedAt(new Date());
        if (isNew) {
            if (approval.getStepOrder() == null) approval.setStepOrder(1);
            if (approval.getStepName() == null) approval.setStepName("单级审批");
        }
    leaveApprovalRepository.save(approval);
        
        // 恢复学生请假余额
        restoreStudentLeaveBalance(leaveRequest);
        LeaveRequest fin = leaveRequestRepository.save(leaveRequest);
    try { notifyLeaveFinal(fin, false, false, comments); } catch (Exception ignored) {}
        return fin;
    }

    // 返回 DTO 版本（供 Controller 新端点使用）
    @Transactional
    public com.altair288.class_management.dto.LeaveRequestListDTO approveLeaveRequestAsDTO(Integer id, Integer approverId, String comments) {
        var lr = approveLeaveRequest(id, approverId, comments);
        return getOneAsDTO(lr.getId());
    }

    @Transactional
    public com.altair288.class_management.dto.LeaveRequestListDTO rejectLeaveRequestAsDTO(Integer id, Integer approverId, String comments) {
        var lr = rejectLeaveRequest(id, approverId, comments);
        return getOneAsDTO(lr.getId());
    }

    @Transactional
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> batchApprove(List<Integer> ids, Integer approverId, String comments) {
        List<com.altair288.class_management.dto.LeaveRequestListDTO> result = new ArrayList<>();
        for (Integer id : ids) {
            try { result.add(approveLeaveRequestAsDTO(id, approverId, comments)); } catch (Exception ignored) {}
        }
        return result;
    }

    @Transactional
    public List<com.altair288.class_management.dto.LeaveRequestListDTO> batchReject(List<Integer> ids, Integer approverId, String comments) {
        List<com.altair288.class_management.dto.LeaveRequestListDTO> result = new ArrayList<>();
        for (Integer id : ids) {
            try { result.add(rejectLeaveRequestAsDTO(id, approverId, comments)); } catch (Exception ignored) {}
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getLeaveStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 基础计数：一次 count 各状态（这里用现有 repository 方法，底层单条 SQL）
        long total = leaveRequestRepository.count();
        long pending = Optional.ofNullable(leaveRequestRepository.countByStatus("待审批")).orElse(0L);
        long approved = Optional.ofNullable(leaveRequestRepository.countByStatus("已批准")).orElse(0L);
        long rejected = Optional.ofNullable(leaveRequestRepository.countByStatus("已拒绝")).orElse(0L);

        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);

        // 审批时长统计：从提交到审核完成（仅取已批准），避免 N+1，仅取必要字段
        var durations = leaveRequestRepository.findApprovalDurations();
        if (durations != null && !durations.isEmpty()) {
            long sumMs = 0L;
            long maxMs = Long.MIN_VALUE;
            long minMs = Long.MAX_VALUE;
            int n = 0;
            for (var d : durations) {
                Date c = d.getCreatedAt();
                Date r = d.getReviewedAt();
                if (c != null && r != null && r.after(c)) {
                    long ms = r.getTime() - c.getTime();
                    sumMs += ms;
                    maxMs = Math.max(maxMs, ms);
                    minMs = Math.min(minMs, ms);
                    n++;
                }
            }
            Map<String, Object> durationStat = new HashMap<>();
            if (n > 0) {
                durationStat.put("avgHours", (double) sumMs / n / 3600000.0);
                durationStat.put("minHours", (double) minMs / 3600000.0);
                durationStat.put("maxHours", (double) maxMs / 3600000.0);
                durationStat.put("count", n);
            } else {
                durationStat.put("avgHours", 0.0);
                durationStat.put("minHours", 0.0);
                durationStat.put("maxHours", 0.0);
                durationStat.put("count", 0);
            }
            stats.put("approvalDuration", durationStat);
        } else {
            stats.put("approvalDuration", Map.of("avgHours", 0.0, "minHours", 0.0, "maxHours", 0.0, "count", 0));
        }

        // 请假类型次数统计：一次聚合查询
        var typeCounts = leaveRequestRepository.countByLeaveTypeGrouped();
        List<Map<String, Object>> typeList = new ArrayList<>();
        if (typeCounts != null) {
            for (var t : typeCounts) {
                Map<String, Object> row = new HashMap<>();
                row.put("typeCode", t.getTypeCode());
                row.put("typeName", t.getTypeName());
                row.put("count", t.getCount());
                typeList.add(row);
            }
        }
        stats.put("typeCounts", typeList);

        return stats;
    }

    public List<LeaveTypeConfig> getLeaveTypes() {
        return leaveTypeConfigRepository.findByEnabledTrueOrderByTypeCode();
    }

    public List<StudentLeaveBalance> getStudentLeaveBalances(Integer studentId, String academicYear) {
        // 解析学年字符串为年份整数
        Integer year = null;
        if (academicYear != null && !academicYear.trim().isEmpty()) {
            if (academicYear.contains("-")) {
                String[] parts = academicYear.split("-");
                if (parts.length > 0) {
                    try {
                        year = Integer.parseInt(parts[0].trim());
                    } catch (NumberFormatException e) {
                        year = 2024; // 默认年份
                    }
                }
            } else {
                try {
                    year = Integer.parseInt(academicYear.trim());
                } catch (NumberFormatException e) {
                    year = 2024; // 默认年份
                }
            }
        }
        
        if (year != null) {
            return studentLeaveBalanceRepository.findByStudentIdAndYear(studentId, year);
        } else {
            return studentLeaveBalanceRepository.findByStudentId(studentId);
        }
    }

    @Transactional
    private void updateStudentLeaveBalance(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveTypeId() == null || leaveRequest.getDays() == null) {
            return;
        }
        
        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Optional<StudentLeaveBalance> balanceOpt = studentLeaveBalanceRepository
            .findByStudentIdAndLeaveTypeIdAndYear(
                leaveRequest.getStudentId(), 
                leaveRequest.getLeaveTypeId(), 
                currentYear
            );
        
        if (balanceOpt.isPresent()) {
            StudentLeaveBalance balance = balanceOpt.get();
            balance.setUsedDays(balance.getUsedDays() + leaveRequest.getDays());
            balance.setRemainingDays(balance.getTotalAllowance() - balance.getUsedDays());
            balance.setUpdatedAt(new Date());
            studentLeaveBalanceRepository.save(balance);
        }
    }

    @Transactional
    private void restoreStudentLeaveBalance(LeaveRequest leaveRequest) {
        if (leaveRequest.getLeaveTypeId() == null || leaveRequest.getDays() == null) {
            return;
        }
        
        Optional<StudentLeaveBalance> balanceOpt = studentLeaveBalanceRepository
            .findByStudentIdAndLeaveTypeIdAndYear(
                leaveRequest.getStudentId(), 
                leaveRequest.getLeaveTypeId(), 
                Calendar.getInstance().get(Calendar.YEAR)
            );
        
        if (balanceOpt.isPresent()) {
            StudentLeaveBalance balance = balanceOpt.get();
            balance.setUsedDays(Math.max(0.0, balance.getUsedDays() - leaveRequest.getDays()));
            balance.setRemainingDays(balance.getTotalAllowance() - balance.getUsedDays());
            balance.setUpdatedAt(new Date());
            studentLeaveBalanceRepository.save(balance);
        }
    }

    public LeaveRequest save(LeaveRequest leaveRequest) {
        leaveRequest.setUpdatedAt(new Date());
        return leaveRequestRepository.save(leaveRequest);
    }

    /**
     * 取消（撤销）一个仍处于“待审批”状态的请假申请：
     * - 恢复已扣减的请假余额
     * - 更新状态为“已撤销”
     * 典型使用场景：前端先提交申请再批量上传附件，若附件上传过程中出现失败需要整体回滚。
     */
    @Transactional
    public LeaveRequest cancelPendingLeaveRequest(Integer id) {
        LeaveRequest lr = getById(id);
        if (lr == null) {
            throw new RuntimeException("请假申请不存在");
        }
        if (!"待审批".equals(lr.getStatus())) {
            // 只有待审批才能撤销，其它状态忽略/抛错视业务需要，这里抛错便于前端感知
            throw new RuntimeException("当前状态不允许撤销: " + lr.getStatus());
        }
        // 恢复余额（与拒绝逻辑一致）
        restoreStudentLeaveBalance(lr);
        lr.setStatus("已撤销");
        lr.setUpdatedAt(new Date());
        return leaveRequestRepository.save(lr);
    }

    // 获取当前登录用户用于自动填充申请单的信息
    @Transactional(readOnly = true)
    public CurrentUserLeaveInfoDTO getCurrentUserLeaveInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("未登录或无效的会话");
        }
        var user = userRepository.findByUsernameOrIdentityNo(auth.getName())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        CurrentUserLeaveInfoDTO dto = new CurrentUserLeaveInfoDTO();
        dto.setUserId(user.getId());
        // 使用 if-else 而不是 switch enum, 避免生成合成内部类 (如 LeaveRequestService$1) 引发在
        // 不一致部署 / 旧类残留 时出现 NoClassDefFoundError。
        String userTypeCn;
        User.UserType ut = user.getUserType();
        if (ut == User.UserType.STUDENT) {
            userTypeCn = "学生";
        } else if (ut == User.UserType.TEACHER) {
            userTypeCn = "教师";
        } else if (ut == User.UserType.PARENT) {
            userTypeCn = "家长";
        } else if (ut == User.UserType.ADMIN) {
            userTypeCn = "管理员";
        } else {
            userTypeCn = "未知";
        }
        dto.setUserType(userTypeCn);


        // 当为学生时：student 信息 + 班级 + 班主任
        if (user.getUserType() == User.UserType.STUDENT) {
            Integer sid = user.getRelatedId();
            var stu = sid == null ? null : studentRepository.findById(sid).orElse(null);
            if (stu != null) {
                dto.setStudentId(stu.getId());
                dto.setStudentName(stu.getName());
                dto.setPhone(stu.getPhone());
                dto.setEmail(stu.getEmail());
                var clazz = stu.getClazz();
                if (clazz != null) {
                    dto.setClassId(clazz.getId());
                    dto.setClassName(clazz.getName());
                    if (clazz.getTeacher() != null) {
                        dto.setTeacherId(clazz.getTeacher().getId());
                        dto.setTeacherName(clazz.getTeacher().getName());
                    }
                }
            }
        } else if (user.getUserType() == User.UserType.TEACHER) {
            Integer tid = user.getRelatedId();
            var t = tid == null ? null : teacherRepository.findById(tid).orElse(null);
            if (t != null) {
                dto.setTeacherId(t.getId());
                dto.setTeacherName(t.getName());
                dto.setPhone(t.getPhone());
                dto.setEmail(t.getEmail());
            }
        }

        return dto;
    }

    // 日历视图数据：一次查询连表返回轻量字段
    @Transactional(readOnly = true)
    public List<LeaveCalendarDTO> getCalendarData(Integer classId, String status, Date start, Date end) {
        var rows = leaveRequestRepository.findForCalendar(classId, status, start, end);
        List<LeaveCalendarDTO> list = new ArrayList<>();
        for (var r : rows) {
            list.add(new LeaveCalendarDTO(
                    r.getId(),
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getStudentNo(),
                    r.getLeaveTypeCode(),
                    r.getLeaveTypeName(),
                    r.getStatus(),
                    r.getStartDate(),
                    r.getEndDate()
            ));
        }
        return list;
    }

    // 学生个人日历视图（不依赖班级过滤，只返回本人记录）
    @Transactional(readOnly = true)
    public List<LeaveCalendarDTO> getStudentCalendarData(Integer studentId, String status, Date start, Date end) {
        if (studentId == null) return java.util.Collections.emptyList();
        var rows = leaveRequestRepository.findForStudentCalendar(studentId, status, start, end);
        List<LeaveCalendarDTO> list = new ArrayList<>();
        for (var r : rows) {
            list.add(new LeaveCalendarDTO(
                    r.getId(),
                    r.getStudentId(),
                    r.getStudentName(),
                    r.getStudentNo(),
                    r.getLeaveTypeCode(),
                    r.getLeaveTypeName(),
                    r.getStatus(),
                    r.getStartDate(),
                    r.getEndDate()
            ));
        }
        return list;
    }
    // =============== 通知辅助方法 ==================
    private void notifyNextStep(LeaveRequest leaveRequest, LeaveApproval pending) {
        if (pending == null || pending.getTeacherId() == null) return;
        var teacherUser = userRepository.findByRelatedIdAndUserType(pending.getTeacherId(), User.UserType.TEACHER);
        if (teacherUser.isEmpty()) return;
        Map<String,Object> vars = buildLeaveVariables(leaveRequest, pending, null);
        notificationService.createFromTemplate(new NotificationService.TemplateRequest(
                NotificationType.LEAVE_STEP_ADVANCED,
                "LEAVE_STEP_ADVANCED_TO_APPROVER",
                vars,
                NotificationPriority.NORMAL,
                "LEAVE_REQUEST",
                String.valueOf(leaveRequest.getId()),
                "leave:step:" + leaveRequest.getId() + ":" + pending.getStepOrder(),
                java.util.List.of(teacherUser.get().getId())
        ));
    }

    private void notifyLeaveFinal(LeaveRequest leaveRequest, boolean approved, boolean autoApproved, String rejectReason) {
        if (leaveRequest.getStudentId() == null) return;
        var stuUser = userRepository.findByRelatedIdAndUserType(leaveRequest.getStudentId(), User.UserType.STUDENT);
        if (stuUser.isEmpty()) return;
        Map<String,Object> vars = buildLeaveVariables(leaveRequest, null, rejectReason);
        String templateCode;
        if (approved) {
            templateCode = autoApproved ? "LEAVE_AUTO_APPROVED_TO_STUDENT" : "LEAVE_APPROVED_TO_STUDENT";
        } else {
            templateCode = "LEAVE_REJECTED_TO_STUDENT";
        }
        notificationService.createFromTemplate(new NotificationService.TemplateRequest(
                approved ? NotificationType.LEAVE_APPROVED : NotificationType.LEAVE_REJECTED,
                templateCode,
                vars,
                approved ? NotificationPriority.NORMAL : NotificationPriority.HIGH,
                "LEAVE_REQUEST",
                String.valueOf(leaveRequest.getId()),
                "leave:final:" + leaveRequest.getId() + ":" + (approved ? "A" : "R"),
                java.util.List.of(stuUser.get().getId())
        ));
    }

    // 组装模板变量
    private Map<String,Object> buildLeaveVariables(LeaveRequest lr, LeaveApproval currentPending, String rejectReason) {
        Map<String,Object> vars = new HashMap<>();
        if (lr == null) return vars;
        vars.put("leaveId", lr.getId());
        vars.put("startDate", formatDate(lr.getStartDate()));
        vars.put("endDate", formatDate(lr.getEndDate()));
        if (lr.getDays() != null) vars.put("days", lr.getDays());
        if (lr.getLeaveTypeId() != null) {
            var cfg = leaveTypeConfigRepository.findById(lr.getLeaveTypeId()).orElse(null);
            if (cfg != null) vars.put("leaveTypeName", safe(cfg.getTypeName()));
        }
        if (lr.getStudentId() != null) {
            var stu = studentRepository.findById(lr.getStudentId()).orElse(null);
            if (stu != null) {
                vars.put("studentName", safe(stu.getName()));
                vars.put("studentNo", safe(stu.getStudentNo()));
                if (stu.getClazz() != null) {
                    vars.put("className", safe(stu.getClazz().getName()));
                    if (stu.getClazz().getDepartment() != null) {
                        vars.put("departmentName", safe(stu.getClazz().getDepartment().getName()));
                    }
                }
            }
        }
        if (currentPending != null) {
            vars.put("currentStepName", safe(currentPending.getStepName()));
        }
        if (rejectReason != null) {
            vars.put("rejectReason", rejectReason);
        }
        return vars;
    }

    private String formatDate(Date d) {
        if (d == null) return null;
        java.time.format.DateTimeFormatter f = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(f);
    }
    private String safe(String s) { return s == null? "": s; }
}
