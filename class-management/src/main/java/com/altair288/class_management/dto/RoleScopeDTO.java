package com.altair288.class_management.dto;

/**
 * 组织角色与作用域
 */
public class RoleScopeDTO {
    private Integer id; // role_assignment id
    private String role; // 班主任 / 系部主任 / 年级主任 / 教务主任 / 校长
    private Integer classId;
    private String className;
    private Integer departmentId;
    private String departmentName;
    private String grade; // 年级作用域
    private String scopeType; // class | department | grade | global

    public RoleScopeDTO() {}

    public RoleScopeDTO(Integer id, String role, Integer classId, String className, Integer departmentId, String departmentName, String grade, String scopeType) {
        this.id = id;
        this.role = role;
        this.classId = classId;
        this.className = className;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.grade = grade;
        this.scopeType = scopeType;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Integer getClassId() { return classId; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
}
