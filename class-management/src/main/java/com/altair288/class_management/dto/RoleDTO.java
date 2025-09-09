package com.altair288.class_management.dto;

import com.altair288.class_management.model.Role;

public class RoleDTO {
    private Integer id;
    private String code;
    private String displayName;
    private String category; // SYSTEM / APPROVAL
    private Integer level;
    private Integer sortOrder;
    private String description;
    private Boolean enabled;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    // usage 统计（删除前前端展示用）
    private Long userCount;          // 绑定该角色的用户数量
    private Long approvalStepCount;  // 作为审批步骤引用次数
    private Long assignmentCount;    // 审批人指派记录数

    public RoleDTO() {}
    public RoleDTO(Role r) {
        this.id = r.getId();
        this.code = r.getCode();
        this.displayName = r.getDisplayName();
        this.category = r.getCategory()!=null? r.getCategory().name():null;
        this.level = r.getLevel();
        this.sortOrder = r.getSortOrder();
        this.description = r.getDescription();
        this.enabled = r.getEnabled();
    this.createdAt = r.getCreatedAt();
    this.updatedAt = r.getUpdatedAt();
    }

    // Getters / Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Long getUserCount() { return userCount; }
    public void setUserCount(Long userCount) { this.userCount = userCount; }
    public Long getApprovalStepCount() { return approvalStepCount; }
    public void setApprovalStepCount(Long approvalStepCount) { this.approvalStepCount = approvalStepCount; }
    public Long getAssignmentCount() { return assignmentCount; }
    public void setAssignmentCount(Long assignmentCount) { this.assignmentCount = assignmentCount; }
}
