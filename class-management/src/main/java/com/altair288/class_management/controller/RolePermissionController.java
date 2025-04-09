// src/main/java/com/altair288/class_management/controller/RolePermissionController.java
package com.altair288.class_management.controller;

import com.altair288.class_management.dto.RolePermissionDTO;
import com.altair288.class_management.model.Permission;
import com.altair288.class_management.model.Role;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.UserRole;
import com.altair288.class_management.model.RolePermission;
import com.altair288.class_management.service.RolePermissionService;
import com.altair288.class_management.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/role-permissions")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;
    private final UserRoleService userRoleService;

    @Autowired
    public RolePermissionController(RolePermissionService rolePermissionService, UserRoleService userRoleService) {
        this.rolePermissionService = rolePermissionService;
        this.userRoleService = userRoleService;
    }

    @PostMapping
    public ResponseEntity<RolePermissionDTO> assignPermission(
        @RequestParam Integer roleId,
        @RequestParam Integer permissionId,
        @RequestParam(required = false) Integer grantedByUserId
    ) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(new Role(roleId));
        rolePermission.setPermission(new Permission(permissionId));
        rolePermission.setGrantedBy(new User(grantedByUserId));

        RolePermission savedRolePermission = rolePermissionService.assignPermissionToRole(rolePermission);

        RolePermissionDTO rolePermissionDTO = new RolePermissionDTO(
            savedRolePermission.getId(),
            savedRolePermission.getRole().getId(),
            savedRolePermission.getRole().getRoleName(),
            savedRolePermission.getPermission().getId(),
            savedRolePermission.getPermission().getPermissionName(),
            savedRolePermission.getGrantedBy() != null ? savedRolePermission.getGrantedBy().getId() : null
        );

        return new ResponseEntity<>(rolePermissionDTO, HttpStatus.CREATED);
    }

    @GetMapping("/role/{roleId}")
    public List<RolePermissionDTO> getPermissionsByRole(@PathVariable Integer roleId) {
        List<RolePermission> rolePermissions = rolePermissionService.getPermissionsByRole(roleId);

        return rolePermissions.stream()
            .map(rolePermission -> new RolePermissionDTO(
                rolePermission.getId(),
                rolePermission.getRole().getId(),
                rolePermission.getRole().getRoleName(),
                rolePermission.getPermission().getId(),
                rolePermission.getPermission().getPermissionName(),
                rolePermission.getGrantedBy() != null ? rolePermission.getGrantedBy().getId() : null
            ))
            .collect(Collectors.toList());
    }

    // 添加获取用户权限的GET方法
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RolePermissionDTO>> getUserPermissions(@PathVariable Integer userId) {
        // 获取用户的所有角色
        List<UserRole> userRoles = userRoleService.getRolesByUser(userId);
        if (userRoles.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // 获取这些角色的所有权限
        List<RolePermission> allPermissions = new ArrayList<>();
        for (UserRole userRole : userRoles) {
            List<RolePermission> rolePermissions = rolePermissionService.getPermissionsByRole(userRole.getRole().getId());
            allPermissions.addAll(rolePermissions);
        }

        // 转换为DTO
        List<RolePermissionDTO> dtos = allPermissions.stream()
            .map(rp -> new RolePermissionDTO(
                rp.getId(),
                rp.getRole().getId(),
                rp.getRole().getRoleName(),
                rp.getPermission().getId(),
                rp.getPermission().getPermissionName(),
                rp.getGrantedBy() != null ? rp.getGrantedBy().getId() : null
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/assign-to-user")
    public ResponseEntity<List<RolePermissionDTO>> assignPermissionsToUser(
            @RequestParam Integer userId,
            @RequestParam List<Integer> permissionIds,
            @RequestParam Integer grantedByUserId) {
        
        List<RolePermission> assignedPermissions = new ArrayList<>();
        
        // 获取用户的角色
        List<UserRole> userRoles = userRoleService.getRolesByUser(userId);
        if (userRoles.isEmpty()) {
            throw new IllegalArgumentException("用户没有分配角色");
        }
        
        // 为用户的每个角色分配权限
        for (UserRole userRole : userRoles) {
            for (Integer permissionId : permissionIds) {
                RolePermission rolePermission = new RolePermission();
                rolePermission.setRole(userRole.getRole());
                rolePermission.setPermission(new Permission(permissionId));
                rolePermission.setGrantedBy(new User(grantedByUserId));
                
                assignedPermissions.add(rolePermissionService.assignPermissionToRole(rolePermission));
            }
        }
        
        // 转换为DTO
        List<RolePermissionDTO> dtos = assignedPermissions.stream()
            .map(rp -> new RolePermissionDTO(
                rp.getId(),
                rp.getRole().getId(),
                rp.getRole().getRoleName(),
                rp.getPermission().getId(),
                rp.getPermission().getPermissionName(),
                rp.getGrantedBy().getId()
            ))
            .collect(Collectors.toList());
            
        return new ResponseEntity<>(dtos, HttpStatus.CREATED);
    }
}