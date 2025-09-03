package com.altair288.class_management.service;

import com.altair288.class_management.model.*;
import com.altair288.class_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class WorkflowConfigService {
    @Autowired private ApprovalWorkflowRepository workflowRepo;
    @Autowired private ApprovalStepRepository stepRepo;
    @Autowired private LeaveTypeWorkflowRepository typeMapRepo;
    @Autowired private RoleAssignmentRepository roleAssignmentRepo;

    // Workflows
    public List<ApprovalWorkflow> listWorkflows() { return workflowRepo.findAll(); }

    public Optional<ApprovalWorkflow> getWorkflow(Integer id) { return workflowRepo.findById(id); }

    public ApprovalWorkflow createWorkflow(ApprovalWorkflow w) { return workflowRepo.save(w); }

    public ApprovalWorkflow updateWorkflow(Integer id, ApprovalWorkflow w) {
        w.setId(id);
        return workflowRepo.save(w);
    }

    public void deleteWorkflow(Integer id) { workflowRepo.deleteById(id); }

    // Steps
    public List<ApprovalStep> listSteps(Integer workflowId) {
        return stepRepo.findEnabledStepsByWorkflow(workflowId);
    }

    public ApprovalStep addStep(Integer workflowId, ApprovalStep s) {
        s.setWorkflowId(workflowId);
        return stepRepo.save(s);
    }

    public ApprovalStep updateStep(Integer stepId, ApprovalStep s) {
        s.setId(stepId);
        return stepRepo.save(s);
    }

    public void deleteStep(Integer stepId) { stepRepo.deleteById(stepId); }

    // Global type binding
    @Transactional
    public LeaveTypeWorkflow upsertTypeBinding(Integer leaveTypeId, Integer workflowId) {
        var existing = typeMapRepo.findActiveByLeaveTypeId(leaveTypeId);
        LeaveTypeWorkflow m = existing.orElseGet(LeaveTypeWorkflow::new);
        m.setLeaveTypeId(leaveTypeId);
        m.setWorkflowId(workflowId);
        m.setConditionExpression(null);
        m.setEnabled(true);
        if (m.getId() == null) m.setCreatedAt(new Date());
        return typeMapRepo.save(m);
    }

    @Transactional
    public void removeTypeBinding(Integer leaveTypeId) {
        typeMapRepo.findActiveByLeaveTypeId(leaveTypeId).ifPresent(typeMapRepo::delete);
    }

    // 已移除班级级别绑定，统一仅按请假类型绑定

    // Role assignments
    public RoleAssignment createRoleAssignment(RoleAssignment ra) { return roleAssignmentRepo.save(ra); }

    public RoleAssignment updateRoleAssignment(Integer id, RoleAssignment ra) { ra.setId(id); return roleAssignmentRepo.save(ra); }

    public void deleteRoleAssignment(Integer id) { roleAssignmentRepo.deleteById(id); }
}
