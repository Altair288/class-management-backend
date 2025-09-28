package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "leave_attachment")
public class LeaveAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "leave_request_id")
    private Integer leaveRequestId;

    @Column(name = "file_object_id")
    private Long fileObjectId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "created_by")
    private Integer createdBy;

    // 关联请假单
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", insertable = false, updatable = false)
    @JsonBackReference
    private LeaveRequest leaveRequest;

    // Getters / Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Integer leaveRequestId) { this.leaveRequestId = leaveRequestId; }
    public Long getFileObjectId() { return fileObjectId; }
    public void setFileObjectId(Long fileObjectId) { this.fileObjectId = fileObjectId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }
    public LeaveRequest getLeaveRequest() { return leaveRequest; }
    public void setLeaveRequest(LeaveRequest leaveRequest) { this.leaveRequest = leaveRequest; }
}
