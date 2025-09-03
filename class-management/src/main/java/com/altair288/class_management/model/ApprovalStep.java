package com.altair288.class_management.model;

import jakarta.persistence.*;

@Entity
@Table(name = "approval_step")
public class ApprovalStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "workflow_id", nullable = false)
    private Integer workflowId;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "approver_role", nullable = false)
    private String approverRole;

    @Column(name = "auto_approve", nullable = false)
    private Boolean autoApprove = false;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getWorkflowId() { return workflowId; }
    public void setWorkflowId(Integer workflowId) { this.workflowId = workflowId; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }
    public Boolean getAutoApprove() { return autoApprove; }
    public void setAutoApprove(Boolean autoApprove) { this.autoApprove = autoApprove; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
