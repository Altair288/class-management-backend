package com.altair288.class_management.security;

import com.altair288.class_management.model.Role;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.StudentRepository;
import com.altair288.class_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component("creditPermission")
public class CreditPermission {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public CreditPermission(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    public boolean canEditStudent(Integer targetStudentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return false;
        String username = auth.getName();
        User user = userRepository.findByUsernameOrIdentityNo(username).orElse(null);
        if (user == null)
            return false;
        Set<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        // Normalize: authorities may be like ROLE_ADMIN
        boolean isAdmin = roles.contains("ROLE_ADMIN") || roles.contains(Role.Codes.ADMIN);
        boolean isTeacher = roles.contains("ROLE_TEACHER") || roles.contains(Role.Codes.TEACHER);
        if (isAdmin || isTeacher)
            return true; // global edit
        boolean isMonitor = roles.contains("ROLE_CLASS_MONITOR") || roles.contains(Role.Codes.CLASS_MONITOR);
        if (!isMonitor)
            return false;
        // monitor: only same class
        if (user.getUserType() != User.UserType.STUDENT || user.getRelatedId() == null)
            return false;
        Student operatorStudent = studentRepository.findById(user.getRelatedId()).orElse(null);
        Student target = studentRepository.findById(targetStudentId).orElse(null);
        if (operatorStudent == null || target == null)
            return false;
        return operatorStudent.getClazz() != null && target.getClazz() != null &&
                operatorStudent.getClazz().getId().equals(target.getClazz().getId());
    }

    public boolean canApplyItemRule() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated())
            return false;
        Set<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        // 角色授权优先
        if (roles.contains("ROLE_ADMIN") || roles.contains(Role.Codes.ADMIN) ||
                roles.contains("ROLE_TEACHER") || roles.contains(Role.Codes.TEACHER)) {
            return true;
        }
        // 兼容尚未授予角色但 userType 已区分的管理员 / 教师账号
        String username = auth.getName();
        User user = userRepository.findByUsernameOrIdentityNo(username).orElse(null);
        if (user == null) return false;
        return user.getUserType() == User.UserType.ADMIN || user.getUserType() == User.UserType.TEACHER;
    }
}
