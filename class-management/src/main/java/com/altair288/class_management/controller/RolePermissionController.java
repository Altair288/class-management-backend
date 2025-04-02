// src/main/java/com/altair288/class_management/controller/RolePermissionController.java
package com.altair288.class_management.controller;

import com.altair288.class_management.dto.RolePermissionDTO;
import com.altair288.class_management.model.Permission;
import com.altair288.class_management.model.Role;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.RolePermission;
import com.altair288.class_management.service.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/role-permissions")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    @Autowired
    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
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
}