// src/main/java/com/altair288/class_management/service/UserService.java
package com.altair288.class_management.service;

import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.StudentRepository;
import com.altair288.class_management.repository.UserRepository;
import com.altair288.class_management.repository.UserRoleRepository;
import com.altair288.class_management.model.UserRole;
import com.altair288.class_management.util.PasswordValidator;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, StudentRepository studentRepository,
                       UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.studentRepository = studentRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (userRepository.existsByIdentityNo(user.getIdentityNo())) {
            throw new IllegalArgumentException("学号或工号已存在");
        }
        
        PasswordValidator.validatePassword(user.getPassword());
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(new Date());
        return userRepository.save(user);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User getUserByUsernameOrIdentityNo(String loginName) {
        return userRepository.findByUsernameOrIdentityNo(loginName)
            .orElseGet(() -> {
                // 如果不是学号/用户名命中，尝试按学生姓名精确匹配（仅当唯一时）
                var students = studentRepository.findByName(loginName.trim());
                if (students.size() == 1) {
                    // 回到 identityNo/username 再查一次
                    String studentNo = students.get(0).getStudentNo();
                    return userRepository.findByUsernameOrIdentityNo(studentNo)
                        .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
                } else if (students.size() > 1) {
                    throw new IllegalArgumentException("存在重复姓名，请使用学号登录");
                } else {
                    throw new IllegalArgumentException("用户不存在");
                }
            });
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

    // 判断用户是否拥有指定角色代码
    public boolean userHasRoleCode(Integer userId, String roleCode) {
        if (userId == null || roleCode == null) return false;
        return userRoleRepository.findByUserId(userId).stream()
                .map(UserRole::getRole)
                .filter(r -> r != null && r.getCode() != null)
                .anyMatch(r -> roleCode.equals(r.getCode()));
    }
}