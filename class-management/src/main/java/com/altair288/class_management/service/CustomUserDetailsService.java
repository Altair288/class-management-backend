package com.altair288.class_management.service;

import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.altair288.class_management.repository.UserRoleRepository;
import com.altair288.class_management.model.UserRole;
import com.altair288.class_management.model.Role;

@Service
// 这个类实现了 UserDetailsService 接口，用于加载用户的详细信息
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    @Lazy
    private UserRepository userRepository;

    @Autowired @Lazy
    private UserRoleRepository userRoleRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrIdentityNo(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 1. 从 user_role 表取角色代码 -> ROLE_<CODE>
        List<UserRole> userRoles = Collections.emptyList();
        try {
            userRoles = userRoleRepository.findByUserId(user.getId());
        } catch (Exception ignored) {}
        Set<String> roleCodes = userRoles.stream()
                .map(ur -> ur.getRole() != null ? ur.getRole().getCode() : null)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));

        // 2. 回退：根据 userType 推断（只有当数据库没有任何角色时）
        if (roleCodes.isEmpty() && user.getUserType() != null) {
            switch (user.getUserType()) {
                case ADMIN -> roleCodes.add(Role.Codes.ADMIN);
                case TEACHER -> roleCodes.add(Role.Codes.TEACHER);
                case STUDENT -> roleCodes.add(Role.Codes.STUDENT);
                case PARENT -> roleCodes.add(Role.Codes.PARENT);
            }
        }

        // 3. 兜底：仍为空给通用 USER（避免无权限导致所有请求被拒）
        if (roleCodes.isEmpty()) {
            roleCodes.add("USER");
        }

        List<GrantedAuthority> authorities = roleCodes.stream()
                .map(code -> code.startsWith("ROLE_") ? code : "ROLE_" + code)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}