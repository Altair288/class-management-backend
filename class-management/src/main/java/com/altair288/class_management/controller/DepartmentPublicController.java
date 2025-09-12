package com.altair288.class_management.controller;

import com.altair288.class_management.model.Department;
import com.altair288.class_management.repository.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对外公开只读接口，供前端下拉列表使用
 */
@RestController
@RequestMapping("/api/department")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://192.168.*:*", "http://172.*:*", "http://10.*:*"}, allowCredentials = "true")
public class DepartmentPublicController {
    private final DepartmentRepository departmentRepository;

    public DepartmentPublicController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping("/list")
    public ResponseEntity<List<Department>> list() {
        return ResponseEntity.ok(departmentRepository.findAll());
    }
}
