package com.altair288.class_management.service;

import com.altair288.class_management.dto.StudentCreditItemDTO;
import com.altair288.class_management.dto.StudentCreditsDTO;
import com.altair288.class_management.model.CreditItem;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.StudentCredit;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.repository.UserRoleRepository;
import com.altair288.class_management.model.Role;
import com.altair288.class_management.repository.StudentCreditRepository;
import com.altair288.class_management.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentCreditService {
    private final StudentRepository studentRepository;
    private final StudentCreditRepository studentCreditRepository;
    private final CreditItemRepository creditItemRepository;
    private final StudentEvaluationService evaluationService;
    private final CreditChangeLogService creditChangeLogService;
    private final com.altair288.class_management.repository.UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public StudentCreditService(StudentRepository studentRepository,
                                StudentCreditRepository studentCreditRepository,
                                CreditItemRepository creditItemRepository,
                                StudentEvaluationService evaluationService,
                                CreditChangeLogService creditChangeLogService,
                                com.altair288.class_management.repository.UserRepository userRepository,
                                UserRoleRepository userRoleRepository) {
        this.studentRepository = studentRepository;
        this.studentCreditRepository = studentCreditRepository;
        this.creditItemRepository = creditItemRepository;
        this.evaluationService = evaluationService;
        this.creditChangeLogService = creditChangeLogService;
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // Helper to get operator snapshot（改进：优先从 user_role 获取系统角色代码快照，无前缀；为空再回退 authority / userType）
    private OperatorSnapshot operator() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginName = auth != null ? auth.getName() : "anonymous"; // 登录标识（现在可能是学号）
        String displayName = loginName; // 真实姓名（后续解析）
        Integer userId = null;
        try {
            boolean anonymous = (loginName == null || loginName.isBlank() || "anonymous".equalsIgnoreCase(loginName) || "anonymousUser".equalsIgnoreCase(loginName));
            String lookup = anonymous ? "admin" : loginName;
            var opt = userRepository.findByUsernameOrIdentityNo(lookup);
            if (opt.isPresent()) {
                var u = opt.get();
                userId = u.getId();
                if (anonymous) loginName = u.getIdentityNo() != null ? u.getIdentityNo() : u.getUsername();
                // 解析真实姓名：优先关联实体，其次 username 字段
                try {
                    if (u.getUserType() != null && u.getRelatedId() != null) {
                        switch (u.getUserType()) {
                            case STUDENT -> {
                                var st = studentRepository.findById(u.getRelatedId()).orElse(null);
                                if (st != null && st.getName() != null) displayName = st.getName();
                            }
                            case TEACHER -> {
                                // teacherRepository 不在本服务中，暂时使用 user.username 作为 teacher 名称，如需严格可注入 TeacherRepository
                                displayName = u.getUsername();
                            }
                            case PARENT -> displayName = u.getUsername();
                            case ADMIN -> displayName = u.getUsername();
                        }
                    } else if (u.getUsername() != null) {
                        displayName = u.getUsername();
                    }
                } catch (Exception ignored) {}
                if (displayName == null || displayName.isBlank()) displayName = loginName;
            }
        } catch (Exception ignored) {}

        // 1) 从 user_role 取 role.code
        String roleCodesCsv = "";
        if (userId != null) {
            try {
                var userRoles = userRoleRepository.findByUserId(userId);
                var codes = userRoles.stream()
                        .map(ur -> ur.getRole() != null ? ur.getRole().getCode() : null)
                        .filter(c -> c != null && !c.isBlank())
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
                if (!codes.isEmpty()) {
                    roleCodesCsv = String.join(",", codes);
                }
            } catch (Exception ignored) {}
        }

        // 2) 回退：若数据库角色为空，用 authorities（去掉 ROLE_ 前缀）
        if (roleCodesCsv.isBlank() && auth != null && auth.getAuthorities() != null) {
            var codes = auth.getAuthorities().stream().map(a -> {
                String raw = a.getAuthority();
                if (raw == null) return null;
                return raw.startsWith("ROLE_") ? raw.substring(5) : raw;
            }).filter(s -> s != null && !s.isBlank()).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
            if (!codes.isEmpty()) roleCodesCsv = String.join(",", codes);
        }

        // 3) 再回退：若依旧为空，根据 userType 推断（ADMIN/TEACHER/STUDENT/PARENT），避免出现 USER
        if ((roleCodesCsv == null || roleCodesCsv.isBlank()) && userId != null) {
            var u = userRepository.findById(userId).orElse(null);
            if (u != null && u.getUserType() != null) {
                roleCodesCsv = switch (u.getUserType()) {
                    case ADMIN -> Role.Codes.ADMIN;
                    case TEACHER -> Role.Codes.TEACHER;
                    case STUDENT -> Role.Codes.STUDENT;
                    case PARENT -> Role.Codes.PARENT;
                };
            }
        }
        return new OperatorSnapshot(userId, loginName, displayName, roleCodesCsv);
    }

    public StudentCreditsDTO getTotalsForStudent(Integer studentId) {
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        double de = sum(studentId, "德");
        double zhi = sum(studentId, "智");
        double ti = sum(studentId, "体");
        double mei = sum(studentId, "美");
        double lao = sum(studentId, "劳");
        return new StudentCreditsDTO(s.getId(), s.getName(), s.getStudentNo(), de, zhi, ti, mei, lao);
    }

    public List<StudentCreditsDTO> getTotalsForClass(Integer classId) {
        List<Student> students = studentRepository.findByClazzId(classId);
        List<StudentCreditsDTO> list = new ArrayList<>();
        for (Student s : students) {
            StudentCreditsDTO dto = getTotalsForStudent(s.getId());
            list.add(dto);
        }
        return list;
    }

    @Transactional
    public void updateScore(Integer studentId, Integer creditItemId, Double delta, String reason) {
        if (delta == null || Objects.equals(delta, 0.0)) return;
        StudentCredit sc = studentCreditRepository.findByStudentAndItem(studentId, creditItemId);
        double oldScore;
        if (sc == null) {
            Student s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
            CreditItem item = creditItemRepository.findById(creditItemId).orElseThrow(() -> new IllegalArgumentException("项目不存在"));
            sc = new StudentCredit();
            sc.setStudent(s);
            sc.setCreditItem(item);
            sc.setScore(0.0);
        }
        oldScore = sc.getScore();
        double newScore = oldScore + delta;
        Double max = sc.getCreditItem().getMaxScore();
        if (max != null && newScore > max) newScore = max;
        if (newScore < 0) newScore = 0;
        sc.setScore(newScore);
        studentCreditRepository.save(sc);
        try { evaluationService.recomputeForStudent(studentId); } catch (Exception ignored) {}
        OperatorSnapshot op = operator();
    creditChangeLogService.logChange(op.userId(), op.loginName(), op.displayName(), op.roleCodesCsv(), sc, oldScore, newScore,
                CreditChangeLogService.ActionType.DELTA, reason, null, null, false);
    }

    public List<StudentCreditItemDTO> listStudentItems(Integer studentId, String category) {
        // ensure student exists
        studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        List<StudentCredit> list = studentCreditRepository.findByStudentAndOptionalCategory(studentId, category);
        List<StudentCreditItemDTO> dtoList = new ArrayList<>();
        for (StudentCredit sc : list) {
            CreditItem item = sc.getCreditItem();
            dtoList.add(new StudentCreditItemDTO(
                    item.getId(),
                    item.getCategory(),
                    item.getItemName(),
                    sc.getScore(),
                    item.getMaxScore(),
                    item.getEnabled(),
                    item.getDescription()
            ));
        }
        return dtoList;
    }

    private double sum(Integer studentId, String category) {
        Double v = studentCreditRepository.sumScoreByStudentAndCategory(studentId, category);
        return v == null ? 0.0 : v;
    }

    @Transactional
    public void setScore(Integer studentId, Integer creditItemId, Double value, String reason) {
        if (value == null) return;
        StudentCredit sc = studentCreditRepository.findByStudentAndItem(studentId, creditItemId);
        double oldScore;
        if (sc == null) {
            Student s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
            CreditItem item = creditItemRepository.findById(creditItemId).orElseThrow(() -> new IllegalArgumentException("项目不存在"));
            sc = new StudentCredit();
            sc.setStudent(s);
            sc.setCreditItem(item);
            sc.setScore(0.0);
        }
        oldScore = sc.getScore();
        double newScore = value;
        Double max = sc.getCreditItem().getMaxScore();
        if (max != null && newScore > max) newScore = max;
        if (newScore < 0) newScore = 0;
        sc.setScore(newScore);
        studentCreditRepository.save(sc);
        try { evaluationService.recomputeForStudent(studentId); } catch (Exception ignored) {}
        OperatorSnapshot op = operator();
    creditChangeLogService.logChange(op.userId(), op.loginName(), op.displayName(), op.roleCodesCsv(), sc, oldScore, newScore,
                CreditChangeLogService.ActionType.SET, reason, null, null, false);
    }

    // 批量聚合：向外暴露仓库聚合结果
    @Transactional(readOnly = true)
    public List<com.altair288.class_management.repository.StudentCreditRepository.StudentTotalsProjection> sumByStudentIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return java.util.List.of();
        return studentCreditRepository.sumByStudentIds(ids);
    }

    @Transactional(readOnly = true)
    public com.altair288.class_management.repository.StudentCreditRepository.CategorySumsProjection sumAllCategories() {
        return studentCreditRepository.sumAllCategories();
    }

    @Transactional(readOnly = true)
    public com.altair288.class_management.repository.StudentCreditRepository.DashboardBucketsProjection countBuckets() {
        return studentCreditRepository.countBuckets();
    }

    /**
     * 将某个主项目的规则应用给所有学生。
     * mode:
     *  - reset: 所有学生该项目的分数重置为 item.initialScore（默认）。
     *  - clamp: 只按 item.maxScore 进行封顶（若降低了最大分），不改变其他分数。
     * 返回受影响记录数。
     */
    @Transactional
    public int applyItemRule(Integer itemId, String mode) {
        CreditItem item = creditItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("项目不存在"));
        String m = (mode == null || mode.isBlank()) ? "reset" : mode.trim().toLowerCase();
        List<StudentCredit> list = studentCreditRepository.findAllByCreditItem_Id(itemId);
        int affected = 0;
        List<Double> oldScores = new ArrayList<>(list.size());
        List<Double> newScores = new ArrayList<>(list.size());
        if ("reset".equals(m)) {
            double init = item.getInitialScore() == null ? 0.0 : item.getInitialScore();
            for (StudentCredit sc : list) {
                double old = sc.getScore();
                double v = init;
                Double max = item.getMaxScore();
                if (max != null && v > max) v = max;
                if (v < 0) v = 0;
                if (!Objects.equals(old, v)) {
                    sc.setScore(v);
                    affected++;
                }
                oldScores.add(old);
                newScores.add(sc.getScore());
            }
            studentCreditRepository.saveAll(list);
        } else if ("clamp".equals(m)) {
            Double max = item.getMaxScore();
            if (max == null) return 0;
            for (StudentCredit sc : list) {
                double old = sc.getScore();
                if (sc.getScore() != null && sc.getScore() > max) {
                    sc.setScore(max);
                    affected++;
                }
                oldScores.add(old);
                newScores.add(sc.getScore());
            }
            studentCreditRepository.saveAll(list);
        } else {
            throw new IllegalArgumentException("不支持的模式: " + mode + "，可选 reset/clamp");
        }
        for (StudentCredit sc : list) {
            try { evaluationService.recomputeForStudent(sc.getStudent().getId()); } catch (Exception ignored) {}
        }
        if (affected > 0) {
            OperatorSnapshot op = operator();
            String batchId = UUID.randomUUID().toString();
            creditChangeLogService.batchLog(op.userId(), op.loginName(), op.displayName(), op.roleCodesCsv(), list, oldScores, newScores,
                    "reset".equals(m) ? CreditChangeLogService.ActionType.RESET : CreditChangeLogService.ActionType.CLAMP,
                    m + " applyItemRule", batchId);
        }
        return affected;
    }

    // simple record for operator snapshot
    public record OperatorSnapshot(Integer userId, String loginName, String displayName, String roleCodesCsv) {}
}
