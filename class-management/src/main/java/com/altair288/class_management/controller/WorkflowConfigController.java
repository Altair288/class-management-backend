package com.altair288.class_management.controller;

import com.altair288.class_management.model.*;
import com.altair288.class_management.service.WorkflowConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://192.168.*:*", "http://172.*:*", "http://10.*:*"}, allowCredentials = "true")
public class WorkflowConfigController {

    @Autowired
    private WorkflowConfigService svc;

    // Workflows
    @GetMapping
    public ResponseEntity<List<ApprovalWorkflow>> listWorkflows() {
        return ResponseEntity.ok(svc.listWorkflows());
    }

    @PostMapping
    public ResponseEntity<ApprovalWorkflow> createWorkflow(@RequestBody ApprovalWorkflow w) {
        return ResponseEntity.ok(svc.createWorkflow(w));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApprovalWorkflow> updateWorkflow(@PathVariable Integer id, @RequestBody ApprovalWorkflow w) {
        return ResponseEntity.ok(svc.updateWorkflow(id, w));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Integer id) {
        svc.deleteWorkflow(id);
        return ResponseEntity.ok().build();
    }

    // Steps
    @GetMapping("/{workflowId}/steps")
    public ResponseEntity<List<ApprovalStep>> listSteps(@PathVariable Integer workflowId) {
        return ResponseEntity.ok(svc.listSteps(workflowId));
    }

    @PostMapping("/{workflowId}/steps")
    public ResponseEntity<ApprovalStep> addStep(@PathVariable Integer workflowId, @RequestBody ApprovalStep s) {
        return ResponseEntity.ok(svc.addStep(workflowId, s));
    }

    @PutMapping("/steps/{stepId}")
    public ResponseEntity<ApprovalStep> updateStep(@PathVariable Integer stepId, @RequestBody ApprovalStep s) {
        return ResponseEntity.ok(svc.updateStep(stepId, s));
    }

    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<Void> deleteStep(@PathVariable Integer stepId) {
        svc.deleteStep(stepId);
        return ResponseEntity.ok().build();
    }

    // Global type binding
    @PutMapping("/bind/type/{leaveTypeId}/workflow/{workflowId}")
    public ResponseEntity<LeaveTypeWorkflow> bindType(@PathVariable Integer leaveTypeId, @PathVariable Integer workflowId) {
        return ResponseEntity.ok(svc.upsertTypeBinding(leaveTypeId, workflowId));
    }

    @DeleteMapping("/bind/type/{leaveTypeId}")
    public ResponseEntity<Void> unbindType(@PathVariable Integer leaveTypeId) {
        svc.removeTypeBinding(leaveTypeId);
        return ResponseEntity.ok().build();
    }

    // 已移除班级级别绑定，统一仅按请假类型绑定

    // Role assignment
    @PostMapping("/role-assignment")
    public ResponseEntity<RoleAssignment> createRoleAssignment(@RequestBody RoleAssignment ra) {
        return ResponseEntity.ok(svc.createRoleAssignment(ra));
    }

    @PutMapping("/role-assignment/{id}")
    public ResponseEntity<RoleAssignment> updateRoleAssignment(@PathVariable Integer id, @RequestBody RoleAssignment ra) {
        return ResponseEntity.ok(svc.updateRoleAssignment(id, ra));
    }

    @DeleteMapping("/role-assignment/{id}")
    public ResponseEntity<Void> deleteRoleAssignment(@PathVariable Integer id) {
        svc.deleteRoleAssignment(id);
        return ResponseEntity.ok().build();
    }
}
