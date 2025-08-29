package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_leave_balance")
public class StudentLeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "leave_type_id")
    private Integer leaveTypeId;

    @Column(name = "year")
    private Integer year;

    @Column(name = "total_allowance")
    private Integer totalAllowance;

    @Column(name = "used_days")
    private Double usedDays = 0.0;

    @Column(name = "remaining_days")
    private Double remainingDays;

    @Column(name = "updated_at")
    private Date updatedAt;

    // 关联实体
    @ManyToOne
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "leave_type_id", insertable = false, updatable = false)
    private LeaveTypeConfig leaveTypeConfig;

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

    public Integer getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Integer leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getTotalAllowance() {
        return totalAllowance;
    }

    public void setTotalAllowance(Integer totalAllowance) {
        this.totalAllowance = totalAllowance;
    }

    public Double getUsedDays() {
        return usedDays;
    }

    public void setUsedDays(Double usedDays) {
        this.usedDays = usedDays;
    }

    public Double getRemainingDays() {
        return remainingDays;
    }

    public void setRemainingDays(Double remainingDays) {
        this.remainingDays = remainingDays;
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

    public LeaveTypeConfig getLeaveTypeConfig() {
        return leaveTypeConfig;
    }

    public void setLeaveTypeConfig(LeaveTypeConfig leaveTypeConfig) {
        this.leaveTypeConfig = leaveTypeConfig;
    }
}
