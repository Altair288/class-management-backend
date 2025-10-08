package com.altair288.class_management.service;

import com.altair288.class_management.model.Role;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ClassMonitorService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final UserRoleRepository userRoleRepository;
    private final OperationLogService operationLogService;

    public ClassMonitorService(StudentRepository studentRepository,
                               UserRepository userRepository,
                               RoleService roleService,
                               UserRoleService userRoleService,
                               UserRoleRepository userRoleRepository,
                               OperationLogService operationLogService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.userRoleRepository = userRoleRepository;
        this.operationLogService = operationLogService;
    }

    public record MonitorInfo(Integer studentId, String studentName, Integer userId, Integer classId) {}
    public record ChangeResult(Integer oldMonitorStudentId, Integer newMonitorStudentId, boolean unchanged) {}

    @Transactional(readOnly = true)
    public Optional<MonitorInfo> getCurrentMonitor(Integer classId) {
        Optional<Integer> userIdOpt = userRoleRepository.findMonitorUserIdByClass(classId);
        if (userIdOpt.isEmpty()) return Optional.empty();
        var userOpt = userRepository.findById(userIdOpt.get());
        if (userOpt.isEmpty()) return Optional.empty();
        User u = userOpt.get();
        if (u.getRelatedId() == null) return Optional.empty();
        Student stu = studentRepository.findById(u.getRelatedId())
                .orElse(null);
        if (stu == null) return Optional.empty();
        return Optional.of(new MonitorInfo(stu.getId(), stu.getName(), u.getId(), stu.getClazz() != null ? stu.getClazz().getId() : null));
    }

    @Transactional
    public ChangeResult setMonitor(Integer classId, Integer studentId) {
        Student stu = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        if (stu.getClazz() == null || !stu.getClazz().getId().equals(classId)) {
            throw new IllegalArgumentException("该学生不属于目标班级");
        }
        var current = getCurrentMonitor(classId);
        if (current.isPresent() && current.get().studentId().equals(studentId)) {
            operationLogService.log("Set class monitor unchanged: classId=" + classId + " studentId=" + studentId);
            return new ChangeResult(current.get().studentId(), studentId, true);
        }
        // 清除旧的
        userRoleRepository.deleteMonitorByClass(classId);
        // 绑定新的
        User u = userRepository.findByRelatedIdAndUserType(stu.getId(), User.UserType.STUDENT)
                .orElseThrow(() -> new IllegalStateException("学生没有用户账号"));
        Role monitorRole = roleService.getByCode(Role.Codes.CLASS_MONITOR);
        userRoleService.assignRoleToUser(u.getId(), monitorRole.getId());
        operationLogService.log("Set class monitor: classId=" + classId + " old=" + current.map(MonitorInfo::studentId).orElse(null) + " new=" + studentId);
        return new ChangeResult(current.map(MonitorInfo::studentId).orElse(null), studentId, false);
    }

    @Transactional
    public boolean removeMonitor(Integer classId) {
        var current = getCurrentMonitor(classId);
        boolean removed = userRoleRepository.deleteMonitorByClass(classId) > 0;
        if (removed) {
            operationLogService.log("Remove class monitor: classId=" + classId + " old=" + current.map(MonitorInfo::studentId).orElse(null));
        }
        return removed;
    }
}
