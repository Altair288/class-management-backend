package com.altair288.class_management.controller;

import com.altair288.class_management.dto.RoleDTO;
import com.altair288.class_management.model.Role;
import com.altair288.class_management.service.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;
    public RoleController(RoleService roleService) { this.roleService = roleService; }

    @GetMapping
    public List<RoleDTO> listAll() {
        return roleService.listAll().stream().map(RoleDTO::new).collect(Collectors.toList());
    }

    // 带 usage 统计的列表（前端删除前确认使用）
    @GetMapping("/with-usage")
    public List<RoleDTO> listAllWithUsage() {
        var roles = roleService.listAll();
        var usage = roleService.computeUsage(roles.stream().map(Role::getId).toList());
        return roles.stream().map(r -> {
            RoleDTO dto = new RoleDTO(r);
            RoleService.UsageStat us = usage.get(r.getId());
            if (us != null) {
                dto.setUserCount(us.userCount);
                dto.setApprovalStepCount(us.approvalStepCount);
                dto.setAssignmentCount(us.assignmentCount);
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @GetMapping("/system")
    public List<RoleDTO> systemRoles() {
        return roleService.listSystemRoles().stream().map(RoleDTO::new).collect(Collectors.toList());
    }

    @GetMapping("/approval")
    public List<RoleDTO> approvalRoles() {
        return roleService.listApprovalRoles().stream().map(RoleDTO::new).collect(Collectors.toList());
    }

    @PostMapping
    public RoleDTO create(@RequestBody RoleDTO dto) {
        Role r = new Role();
        r.setCode(dto.getCode());
        r.setDisplayName(dto.getDisplayName());
        r.setCategory(Role.Category.valueOf(dto.getCategory()));
        if (dto.getLevel()!=null) r.setLevel(dto.getLevel());
        if (dto.getSortOrder()!=null) r.setSortOrder(dto.getSortOrder());
        r.setDescription(dto.getDescription());
        if (dto.getEnabled()!=null) r.setEnabled(dto.getEnabled());
        return new RoleDTO(roleService.createRole(r));
    }

    @PutMapping("/{id}")
    public RoleDTO update(@PathVariable Integer id, @RequestBody RoleDTO dto) {
        Role updated = roleService.update(id, r -> {
            if (dto.getDisplayName()!=null) r.setDisplayName(dto.getDisplayName());
            if (dto.getDescription()!=null) r.setDescription(dto.getDescription());
            if (dto.getEnabled()!=null) r.setEnabled(dto.getEnabled());
            if (dto.getLevel()!=null) r.setLevel(dto.getLevel());
            if (dto.getSortOrder()!=null) r.setSortOrder(dto.getSortOrder());
        });
        return new RoleDTO(updated);
    }

    @PatchMapping("/{id}/hierarchy")
    public RoleDTO updateHierarchy(@PathVariable Integer id,
                                   @RequestParam(required = false) Integer level,
                                   @RequestParam(required = false) Integer sortOrder) {
        return new RoleDTO(roleService.updateHierarchy(id, level, sortOrder));
    }

    @PatchMapping("/{id}/enabled")
    public RoleDTO toggle(@PathVariable Integer id, @RequestParam Boolean enabled) {
        return new RoleDTO(roleService.toggleEnable(id, enabled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 批量层级/排序更新
    @PatchMapping("/hierarchy/batch")
    public ResponseEntity<Void> batchHierarchy(@RequestBody List<RoleService.HierarchyPatch> patches) {
        roleService.batchUpdateHierarchy(patches);
        return ResponseEntity.ok().build();
    }
}
