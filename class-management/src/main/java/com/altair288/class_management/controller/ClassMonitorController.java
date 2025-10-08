package com.altair288.class_management.controller;

import com.altair288.class_management.service.ClassMonitorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes/{classId}/monitor")
public class ClassMonitorController {

    private final ClassMonitorService classMonitorService;

    public ClassMonitorController(ClassMonitorService classMonitorService) {
        this.classMonitorService = classMonitorService;
    }

    // 获取当前班长
    @GetMapping
    @PreAuthorize("@classMonitorPermission.canView(#classId)")
    public ResponseEntity<?> getMonitor(@PathVariable Integer classId) {
        return classMonitorService.getCurrentMonitor(classId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    public record SetMonitorRequest(Integer studentId) {}

    // 设置 / 更换班长
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> setMonitor(@PathVariable Integer classId,
                                        @RequestBody SetMonitorRequest req) {
        if (req == null || req.studentId() == null) {
            throw new IllegalArgumentException("studentId 不能为空");
        }
        var result = classMonitorService.setMonitor(classId, req.studentId());
        return ResponseEntity.ok(result);
    }

    // 取消班长（清空）
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<?> removeMonitor(@PathVariable Integer classId) {
        boolean removed = classMonitorService.removeMonitor(classId);
        return ResponseEntity.ok(java.util.Map.of("removed", removed));
    }
}
