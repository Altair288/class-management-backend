package com.altair288.class_management.dto;

import java.util.List;

/**
 * 更新教师：班主任班级 + 组织角色（完整覆盖式更新）
 */
public class UpdateTeacherRolesRequest {
    private Integer homeroomClassId; // 为空表示取消班主任职务
    private List<RoleAssignmentInput> roles; // 需要保留/配置的角色集合

    public Integer getHomeroomClassId() { return homeroomClassId; }
    public void setHomeroomClassId(Integer homeroomClassId) { this.homeroomClassId = homeroomClassId; }
    public List<RoleAssignmentInput> getRoles() { return roles; }
    public void setRoles(List<RoleAssignmentInput> roles) { this.roles = roles; }

    public static class RoleAssignmentInput {
        private Integer id; // 若存在则更新，否则创建
        private String role;
        private Integer classId;
        private Integer departmentId;
        private String grade;
        private Boolean enabled = true;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Integer getClassId() { return classId; }
        public void setClassId(Integer classId) { this.classId = classId; }
        public Integer getDepartmentId() { return departmentId; }
        public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    }
}
