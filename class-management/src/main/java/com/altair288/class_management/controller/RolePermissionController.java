// src/main/java/com/altair288/class_management/controller/RolePermissionController.java
package com.altair288.class_management.controller;

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

@RestController
@RequestMapping("/api/role-permissions")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;

    @Autowired
    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @PostMapping
    public ResponseEntity<RolePermission> assignPermission(
        @RequestParam Integer roleId,
        @RequestParam Integer permissionId,
        @RequestParam(required = false) Integer grantedByUserId
    ) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRole(new Role(roleId));
        rolePermission.setPermission(new Permission(permissionId));
        rolePermission.setGrantedBy(new User(grantedByUserId));
        return new ResponseEntity<>(
            rolePermissionService.assignPermissionToRole(rolePermission),
            HttpStatus.CREATED
        );
    }

    @GetMapping("/role/{roleId}")
    public List<RolePermission> getPermissionsByRole(@PathVariable Integer roleId) {
        return rolePermissionService.getPermissionsByRole(roleId);
    }
}