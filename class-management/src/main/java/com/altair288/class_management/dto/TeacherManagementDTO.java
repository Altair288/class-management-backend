package com.altair288.class_management.dto;

import java.util.List;

/**
 * 教师管理视图 DTO
 * 包含：基本信息 + 班主任班级（若有）+ 所属系部/年级（取班主任班级）+ 组织角色列表
 */
public class TeacherManagementDTO {
    private Integer id;
    private String name;
    private String teacherNo;
    private String phone;
    private String email;

    private Integer homeroomClassId; // 班主任班级ID（若为班主任）
    private String homeroomClassName;
    private String grade; // 班主任班级年级
    private Integer departmentId; // 班主任班级所属系部
    private String departmentName;
    private String departmentCode;

    private List<RoleScopeDTO> roles; // 组织角色（含作用域信息）

    public TeacherManagementDTO() {}

    public TeacherManagementDTO(Integer id, String name, String teacherNo, String phone, String email) {
        this.id = id;
        this.name = name;
        this.teacherNo = teacherNo;
        this.phone = phone;
        this.email = email;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeacherNo() { return teacherNo; }
    public void setTeacherNo(String teacherNo) { this.teacherNo = teacherNo; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getHomeroomClassId() { return homeroomClassId; }
    public void setHomeroomClassId(Integer homeroomClassId) { this.homeroomClassId = homeroomClassId; }
    public String getHomeroomClassName() { return homeroomClassName; }
    public void setHomeroomClassName(String homeroomClassName) { this.homeroomClassName = homeroomClassName; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getDepartmentCode() { return departmentCode; }
    public void setDepartmentCode(String departmentCode) { this.departmentCode = departmentCode; }
    public List<RoleScopeDTO> getRoles() { return roles; }
    public void setRoles(List<RoleScopeDTO> roles) { this.roles = roles; }
}
