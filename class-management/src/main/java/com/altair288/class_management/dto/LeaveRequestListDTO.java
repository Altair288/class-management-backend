package com.altair288.class_management.dto;

import java.util.Date;
import java.util.List;

public class LeaveRequestListDTO {
    private Integer id;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private Integer leaveTypeId;
    private String leaveTypeName;
    private String status;
    private Date startDate;
    private Date endDate;
    private Double days;
    private Date createdAt;
    private Date reviewedAt;
    private String currentStepName; // 当前或最近审批节点
    private String pendingRoleCode;
    private String pendingRoleDisplayName;
    private List<LeaveApprovalDTO> approvals; // 可选（列表页是否需要全部审批记录）
    // 新增：请假原因
    private String reason;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public Integer getLeaveTypeId() { return leaveTypeId; }
    public void setLeaveTypeId(Integer leaveTypeId) { this.leaveTypeId = leaveTypeId; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public void setLeaveTypeName(String leaveTypeName) { this.leaveTypeName = leaveTypeName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public Double getDays() { return days; }
    public void setDays(Double days) { this.days = days; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Date reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getCurrentStepName() { return currentStepName; }
    public void setCurrentStepName(String currentStepName) { this.currentStepName = currentStepName; }
    public String getPendingRoleCode() { return pendingRoleCode; }
    public void setPendingRoleCode(String pendingRoleCode) { this.pendingRoleCode = pendingRoleCode; }
    public String getPendingRoleDisplayName() { return pendingRoleDisplayName; }
    public void setPendingRoleDisplayName(String pendingRoleDisplayName) { this.pendingRoleDisplayName = pendingRoleDisplayName; }
    public List<LeaveApprovalDTO> getApprovals() { return approvals; }
    public void setApprovals(List<LeaveApprovalDTO> approvals) { this.approvals = approvals; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
