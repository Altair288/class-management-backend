package com.altair288.class_management.service;

import com.altair288.class_management.model.Role;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.UserRole;
import com.altair288.class_management.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserRoleService {
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public UserRole assignRoleToUser(Integer userId, Integer roleId) {
        UserRole userRole = new UserRole();
        userRole.setUser(new User(userId));  // 假设User有构造函数接受id
        userRole.setRole(new Role(roleId)); // 假设Role有构造函数接受id
        return userRoleRepository.save(userRole);
    }

    public List<UserRole> getRolesByUser(Integer userId) {
        return userRoleRepository.findByUserId(userId);
    }
}