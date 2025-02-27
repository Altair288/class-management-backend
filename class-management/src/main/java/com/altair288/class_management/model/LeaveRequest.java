package com.altair288.class_management.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "leave_type", nullable = false)
    private String leaveType;

    @Column(name = "leave_reason", nullable = false)
    private String leaveReason;

    @Column(name = "leave_start_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date leaveStartDate;

    @Column(name = "leave_end_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date leaveEndDate;

    @Column(name = "status", nullable = false)
    private String status = "待审批";  // 默认状态

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "approval_date")
    @Temporal(TemporalType.DATE)
    private Date approvalDate;

    // **构造方法**
    public LeaveRequest() {
    }

    public LeaveRequest(Long studentId, String leaveType, String leaveReason, Date leaveStartDate, Date leaveEndDate) {
        this.studentId = studentId;
        this.leaveType = leaveType;
        this.leaveReason = leaveReason;
        this.leaveStartDate = leaveStartDate;
        this.leaveEndDate = leaveEndDate;
        this.status = "待审批";  // 初始化状态
    }

    // **Getter 和 Setter 方法**
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(String leaveType) {
        this.leaveType = leaveType;
    }

    public String getLeaveReason() {
        return leaveReason;
    }

    public void setLeaveReason(String leaveReason) {
        this.leaveReason = leaveReason;
    }

    public Date getLeaveStartDate() {
        return leaveStartDate;
    }

    public void setLeaveStartDate(Date leaveStartDate) {
        this.leaveStartDate = leaveStartDate;
    }

    public Date getLeaveEndDate() {
        return leaveEndDate;
    }

    public void setLeaveEndDate(Date leaveEndDate) {
        this.leaveEndDate = leaveEndDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Date getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }
}
