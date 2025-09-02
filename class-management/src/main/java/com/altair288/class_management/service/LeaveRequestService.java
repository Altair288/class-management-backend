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

    @Transactional
    public LeaveRequest submitLeaveRequest(LeaveRequest leaveRequest) {
        // 设置基本信息
        leaveRequest.setStatus("待审批");
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

        // 自动创建一条待审批记录，指向学生班主任
        try {
            if (saved.getStudentId() != null) {
                var stuOpt = studentRepository.findById(saved.getStudentId());
                if (stuOpt.isPresent()) {
                    var clazz = stuOpt.get().getClazz();
                    if (clazz != null && clazz.getTeacher() != null && clazz.getTeacher().getId() != null) {
                        Integer approverId = clazz.getTeacher().getId();
                        LeaveApproval pending = new LeaveApproval();
                        pending.setLeaveId(saved.getId());
                        pending.setTeacherId(approverId);
                        pending.setStatus("待审批");
                        pending.setReviewedAt(null);
                        pending.setComment(null);
                        leaveApprovalRepository.save(pending);
                    }
                }
            }
        } catch (Exception ignored) {}
        
        return saved;
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
        return leaveRequestRepository.findAll();
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
        
        leaveRequest.setStatus("已批准");
        leaveRequest.setUpdatedAt(new Date());
        
    // 更新或创建审批记录（优先更新提交时的待审批记录）
    var exist = leaveApprovalRepository.findByLeaveIdAndTeacherId(id, approverId);
    LeaveApproval approval = exist.orElseGet(LeaveApproval::new);
    approval.setLeaveId(id);
    approval.setTeacherId(approverId);
    approval.setStatus("已批准");
    approval.setComment(comments);
    approval.setReviewedAt(new Date());
    leaveApprovalRepository.save(approval);
        
        return leaveRequestRepository.save(leaveRequest);
    }

    @Transactional
    public LeaveRequest rejectLeaveRequest(Integer id, Integer approverId, String comments) {
        LeaveRequest leaveRequest = getById(id);
        if (leaveRequest == null) {
            throw new RuntimeException("请假申请不存在");
        }
        
        leaveRequest.setStatus("已拒绝");
        leaveRequest.setUpdatedAt(new Date());
        
    // 更新或创建审批记录
    var exist = leaveApprovalRepository.findByLeaveIdAndTeacherId(id, approverId);
    LeaveApproval approval = exist.orElseGet(LeaveApproval::new);
    approval.setLeaveId(id);
    approval.setTeacherId(approverId);
    approval.setStatus("已拒绝");
    approval.setComment(comments);
    approval.setReviewedAt(new Date());
    leaveApprovalRepository.save(approval);
        
        // 恢复学生请假余额
        restoreStudentLeaveBalance(leaveRequest);
        
        return leaveRequestRepository.save(leaveRequest);
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
        String userTypeCn = switch (user.getUserType()) {
            case STUDENT -> "学生";
            case TEACHER -> "教师";
            case PARENT -> "家长";
            case ADMIN -> "管理员";
        };
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
}
