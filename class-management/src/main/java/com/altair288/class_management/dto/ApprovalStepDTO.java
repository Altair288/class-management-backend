package com.altair288.class_management.dto;

public class ApprovalStepDTO {
    private Integer id;
    private Integer workflowId;
    private Integer stepOrder;
    private String stepName;
    private String roleCode;
    private String roleDisplayName;
    private Boolean autoApprove;
    private Boolean enabled;

    public ApprovalStepDTO() {}

    public ApprovalStepDTO(com.altair288.class_management.model.ApprovalStep s) {
        this.id = s.getId();
        this.workflowId = s.getWorkflowId();
        this.stepOrder = s.getStepOrder();
        this.stepName = s.getStepName();
        if (s.getApproverRole()!=null) {
            this.roleCode = s.getApproverRole().getCode();
            this.roleDisplayName = s.getApproverRole().getDisplayName();
        }
        this.autoApprove = s.getAutoApprove();
        this.enabled = s.getEnabled();
    }

    public Integer getId() { return id; }
    public Integer getWorkflowId() { return workflowId; }
    public Integer getStepOrder() { return stepOrder; }
    public String getStepName() { return stepName; }
    public String getRoleCode() { return roleCode; }
    public String getRoleDisplayName() { return roleDisplayName; }
    public Boolean getAutoApprove() { return autoApprove; }
    public Boolean getEnabled() { return enabled; }
}
