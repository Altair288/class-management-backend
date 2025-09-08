package com.altair288.class_management.controller;

import com.altair288.class_management.dto.TeacherManagementDTO;
import com.altair288.class_management.dto.UpdateTeacherRolesRequest;
import com.altair288.class_management.service.TeacherManagementService;
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
}
