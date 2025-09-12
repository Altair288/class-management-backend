package com.altair288.class_management.service;

import com.altair288.class_management.model.*;
import com.altair288.class_management.repository.*;
import com.altair288.class_management.dto.ApprovalStepRequest;
import com.altair288.class_management.dto.ApprovalStepDTO;
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
    @Autowired private RoleRepository roleRepository;

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

    public ApprovalStepDTO addStep(Integer workflowId, ApprovalStepRequest req) {
        // workflow 校验
        if (workflowRepo.findById(workflowId).isEmpty()) throw new IllegalArgumentException("工作流不存在: " + workflowId);
        if (req.getStepName()==null || req.getStepName().isBlank()) throw new IllegalArgumentException("stepName 不能为空");
        if (req.getRoleCode()==null || req.getRoleCode().isBlank()) throw new IllegalArgumentException("roleCode 不能为空");
        Role role = resolveApprovalRole(req.getRoleCode());
        Integer desiredOrder = req.getStepOrder();
        if (desiredOrder == null) {
            Integer max = stepRepo.findMaxOrder(workflowId);
            desiredOrder = (max==null?0:max) + 1;
        } else {
            final Integer finalOrder = desiredOrder;
            boolean conflict = stepRepo.findByWorkflowIdOrderByStepOrderAsc(workflowId).stream()
                    .anyMatch(s -> s.getStepOrder().equals(finalOrder));
            if (conflict) throw new IllegalArgumentException("stepOrder 已存在: " + desiredOrder);
        }
        ApprovalStep s = new ApprovalStep();
        s.setWorkflowId(workflowId);
    s.setStepOrder(desiredOrder);
        s.setStepName(req.getStepName());
        s.setApproverRole(role);
        s.setAutoApprove(Boolean.TRUE.equals(req.getAutoApprove()));
        s.setEnabled(req.getEnabled()==null?Boolean.TRUE:req.getEnabled());
        return new ApprovalStepDTO(stepRepo.save(s));
    }

    public ApprovalStepDTO updateStep(Integer stepId, ApprovalStepRequest req) {
        ApprovalStep s = stepRepo.findById(stepId).orElseThrow(() -> new IllegalArgumentException("步骤不存在: " + stepId));
        if (req.getStepName()!=null && !req.getStepName().isBlank()) s.setStepName(req.getStepName());
        if (req.getRoleCode()!=null && !req.getRoleCode().isBlank()) {
            Role role = resolveApprovalRole(req.getRoleCode());
            s.setApproverRole(role);
        }
        if (req.getAutoApprove()!=null) s.setAutoApprove(req.getAutoApprove());
        if (req.getEnabled()!=null) s.setEnabled(req.getEnabled());
        if (req.getStepOrder()!=null && !req.getStepOrder().equals(s.getStepOrder())) {
            Integer newOrder = req.getStepOrder();
            boolean conflict = stepRepo.findByWorkflowIdOrderByStepOrderAsc(s.getWorkflowId()).stream()
                    .anyMatch(x -> !x.getId().equals(s.getId()) && x.getStepOrder().equals(newOrder));
            if (conflict) throw new IllegalArgumentException("stepOrder 已存在: " + newOrder);
            s.setStepOrder(newOrder);
        }
        return new ApprovalStepDTO(stepRepo.save(s));
    }

    public void deleteStep(Integer stepId) { stepRepo.deleteById(stepId); }

    // Global type binding
    public List<LeaveTypeWorkflow> listTypeBindings() {
        return typeMapRepo.findAll();
    }

    public Optional<LeaveTypeWorkflow> getTypeBinding(Integer leaveTypeId) {
        return typeMapRepo.findActiveByLeaveTypeId(leaveTypeId);
    }

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

    private Role resolveApprovalRole(String roleCode) {
        Role r = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + roleCode));
        if (r.getCategory() != Role.Category.APPROVAL) {
            throw new IllegalArgumentException("角色不是审批类别: " + roleCode);
        }
        if (r.getEnabled() != null && !r.getEnabled()) {
            throw new IllegalArgumentException("角色已被禁用: " + roleCode);
        }
        return r;
    }
}
