package com.altair288.class_management.controller;

import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Department;
import com.altair288.class_management.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://192.168.*:*", "http://172.*:*", "http://10.*:*"}, allowCredentials = "true")
public class DepartmentController {
    @Autowired private DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<List<Department>> list() { return ResponseEntity.ok(departmentService.list()); }

    @PostMapping
    public ResponseEntity<Department> create(@RequestBody Department d) { return ResponseEntity.ok(departmentService.create(d)); }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@PathVariable Integer id, @RequestBody Department d) { return ResponseEntity.ok(departmentService.update(id, d)); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) { departmentService.delete(id); return ResponseEntity.ok().build(); }

    @PutMapping("/classes/{classId}/bind/{departmentId}")
    public ResponseEntity<Class> bindClass(@PathVariable Integer classId, @PathVariable Integer departmentId) {
        return ResponseEntity.ok(departmentService.bindClass(classId, departmentId));
    }
}
