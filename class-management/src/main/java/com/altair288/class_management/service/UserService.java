// src/main/java/com/altair288/class_management/service/UserService.java
package com.altair288.class_management.service;

import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.UserRepository;
import com.altair288.class_management.repository.UserRoleRepository;
import com.altair288.class_management.model.UserRole;
import com.altair288.class_management.util.PasswordValidator;
import com.altair288.class_management.exception.BusinessException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
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

    /**
     * 修改密码：校验旧密码 -> 校验新密码复杂度 -> 不允许与旧密码相同 -> 持久化
     * @param userId 当前用户ID
     * @param oldPassword 旧密码明文
     * @param newPassword 新密码明文
     */
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        if (userId == null) throw new BusinessException("USER_NOT_FOUND", "非法用户");
        if (oldPassword == null || newPassword == null) throw new BusinessException("PASSWORD_EMPTY", "密码不能为空");
        User user = getUserById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("OLD_PASSWORD_INCORRECT", "旧密码不正确");
        }
        try {
            PasswordValidator.validatePassword(newPassword);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("PASSWORD_POLICY_VIOLATION", ex.getMessage());
        }
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException("PASSWORD_SAME", "新密码不能与旧密码相同");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}