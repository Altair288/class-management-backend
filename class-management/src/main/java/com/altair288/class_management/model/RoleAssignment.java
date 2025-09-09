package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "role_assignment")
public class RoleAssignment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 新结构：approval_role_id 外键指向 role (category=APPROVAL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_role_id", nullable = false)
    private Role approvalRole;

    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;

    @Column(name = "class_id")
    private Integer classId;

    @Column(name = "department_id")
    private Integer departmentId;

    @Column(name = "grade")
    private String grade;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Role getApprovalRole() { return approvalRole; }
    public void setApprovalRole(Role approvalRole) { this.approvalRole = approvalRole; }
    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
