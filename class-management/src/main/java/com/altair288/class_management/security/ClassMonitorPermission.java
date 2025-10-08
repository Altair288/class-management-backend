package com.altair288.class_management.security;

import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.Role;
import com.altair288.class_management.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component("classMonitorPermission")
public class ClassMonitorPermission {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ClassRepository classRepository;

    public ClassMonitorPermission(UserRepository userRepository,
                                  StudentRepository studentRepository,
                                  ClassRepository classRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
    }

    public boolean canView(Integer classId) {
        if (classId == null) return false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        Set<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        boolean isAdmin = roles.contains("ROLE_ADMIN") || roles.contains(Role.Codes.ADMIN);
        if (isAdmin) return true;
        String username = auth.getName();
        User user = userRepository.findByUsernameOrIdentityNo(username).orElse(null);
        if (user == null) return false;
        // teacher (班主任)
        boolean isTeacher = roles.contains("ROLE_TEACHER") || roles.contains(Role.Codes.TEACHER);
        if (isTeacher && user.getUserType() == User.UserType.TEACHER && user.getRelatedId() != null) {
            Class clazz = classRepository.findById(classId).orElse(null);
            if (clazz != null && clazz.getTeacher() != null && clazz.getTeacher().getId().equals(user.getRelatedId())) {
                return true;
            }
        }
        // class monitor of same class
        boolean isMonitor = roles.contains("ROLE_CLASS_MONITOR") || roles.contains(Role.Codes.CLASS_MONITOR);
        if (isMonitor && user.getUserType() == User.UserType.STUDENT && user.getRelatedId() != null) {
            Student stu = studentRepository.findById(user.getRelatedId()).orElse(null);
            if (stu != null && stu.getClazz() != null && stu.getClazz().getId().equals(classId)) {
                return true;
            }
        }
        return false;
    }
}
