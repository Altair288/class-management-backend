// src/main/java/com/altair288/class_management/service/RolePermissionService.java
package com.altair288.class_management.service;

import com.altair288.class_management.model.RolePermission;
import com.altair288.class_management.repository.RolePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RolePermissionService {
    private final RolePermissionRepository rolePermissionRepository;

    @Autowired
    public RolePermissionService(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    // 为角色分配权限
    public RolePermission assignPermissionToRole(RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    // 查询角色的所有权限
    public List<RolePermission> getPermissionsByRole(Integer roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    // 查询权限被哪些角色拥有
    public List<RolePermission> getRolesByPermission(Integer permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId);
    }
}