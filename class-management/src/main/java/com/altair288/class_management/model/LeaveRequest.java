package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "leave_request")
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    // 去除 teacher_id：审批人改由 leave_approval 记录

    @Column(name = "leave_type_id")
    private Integer leaveTypeId;

    @Column(name = "reason")
    private String reason;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "days")
    private Double days;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "emergency_phone")
    private String emergencyPhone;

    @Column(name = "handover_notes")
    private String handoverNotes;

    @Column(name = "attachment_count")
    private Integer attachmentCount = 0;

    @Column(name = "status")
    private String status;

    @Column(name = "reviewed_at")
    private Date reviewedAt;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // 关联实体
    @ManyToOne
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    // 去除对 Teacher 的直接关联，由 LeaveApproval 承担审批人与请假单的关系

    @ManyToOne
    @JoinColumn(name = "leave_type_id", insertable = false, updatable = false)
    private LeaveTypeConfig leaveTypeConfig;

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LeaveAttachment> attachments;

    @OneToMany(mappedBy = "leaveRequest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LeaveApproval> approvals;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getStudentId() {
        return studentId;
    }

    public void setStudentId(Integer studentId) {
        this.studentId = studentId;
    }

    // 移除 teacherId 的 Getter/Setter

    public Integer getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Integer leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getDays() {
        return days;
    }

    public void setDays(Double days) {
        this.days = days;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getHandoverNotes() {
        return handoverNotes;
    }

    public void setHandoverNotes(String handoverNotes) {
        this.handoverNotes = handoverNotes;
    }

    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Date reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    // 移除与 Teacher 的直接映射 Getter/Setter

    public LeaveTypeConfig getLeaveTypeConfig() {
        return leaveTypeConfig;
    }

    public void setLeaveTypeConfig(LeaveTypeConfig leaveTypeConfig) {
        this.leaveTypeConfig = leaveTypeConfig;
    }

    public List<LeaveAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<LeaveAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<LeaveApproval> getApprovals() {
        return approvals;
    }

    public void setApprovals(List<LeaveApproval> approvals) {
        this.approvals = approvals;
    }
}
