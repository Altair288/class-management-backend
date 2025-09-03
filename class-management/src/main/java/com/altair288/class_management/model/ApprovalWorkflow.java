package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "approval_workflow")
public class ApprovalWorkflow {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "workflow_name", nullable = false)
	private String workflowName;

	@Column(name = "workflow_code", nullable = false, unique = true)
	private String workflowCode;

	@Column(name = "description")
	private String description;

	@Column(name = "enabled", nullable = false)
	private Boolean enabled = true;

	@Column(name = "created_at")
	private Date createdAt;

	@Column(name = "updated_at")
	private Date updatedAt;

	public Integer getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	public String getWorkflowName() { return workflowName; }
	public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }
	public String getWorkflowCode() { return workflowCode; }
	public void setWorkflowCode(String workflowCode) { this.workflowCode = workflowCode; }
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	public Boolean getEnabled() { return enabled; }
	public void setEnabled(Boolean enabled) { this.enabled = enabled; }
	public Date getCreatedAt() { return createdAt; }
	public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
	public Date getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
