package com.altair288.class_management.controller;

import com.altair288.class_management.dto.TeacherManagementDTO;
import com.altair288.class_management.dto.UpdateTeacherRolesRequest;
import com.altair288.class_management.service.TeacherManagementService;
import com.altair288.class_management.dto.RoleDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers/management")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://192.168.*:*", "http://172.*:*", "http://10.*:*"}, allowCredentials = "true")
public class TeacherManagementController {
    private final TeacherManagementService service;

    public TeacherManagementController(TeacherManagementService service) { this.service = service; }

    @GetMapping
    public List<TeacherManagementDTO> list() { return service.listAll(); }

    @GetMapping("/{id}")
    public TeacherManagementDTO get(@PathVariable Integer id) { return service.getOne(id); }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherManagementDTO> update(@PathVariable Integer id, @RequestBody UpdateTeacherRolesRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    // 可分配的审批角色（含层级/排序）
    @GetMapping("/assignable-roles")
    public List<RoleDTO> assignableRoles() {
        return service.listAssignableApprovalRoles();
    }

    // 角色层级结构（按 level, sortOrder 排序）
    @GetMapping("/role-hierarchy")
    public List<RoleDTO> roleHierarchy() { return service.roleHierarchy(); }

    // 作用域集合（班级/系部/年级）
    @GetMapping("/scopes")
    public TeacherManagementService.ScopesDTO scopes() { return service.scopes(); }
}
