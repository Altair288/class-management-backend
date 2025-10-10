package com.altair288.class_management.dto;

import com.altair288.class_management.model.User.UserType;

public class UserDTO {
    private Integer id;
    private String username;
    // 新增：loginName (登录使用的标识 - 学号/工号/手机号)，displayName (展示姓名)
    private String loginName;
    private String displayName;
    private UserType userType;
    // 是否为班长（当前用户拥有 CLASS_MONITOR 角色）
    private Boolean classMonitor;
    // 若是班长，其所负责的班级ID（基于其自身 student.clazz.id）
    private Integer monitorClassId;
    // 关联实体 ID：学生->studentId, 老师->teacherId, 家长->parentId（用于前端权限判断）
    private Integer relatedId;
    // 联系方式（从关联实体派生，可为空）
    private String phone;
    private String email;

    public UserDTO() {}

    // 基础构造函数（保持向后兼容）
    public UserDTO(Integer id, String username, UserType userType) {
        this.id = id;
        this.username = username;
        this.userType = userType;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getLoginName() { return loginName; }
    public void setLoginName(String loginName) { this.loginName = loginName; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public Boolean getClassMonitor() { return classMonitor; }
    public void setClassMonitor(Boolean classMonitor) { this.classMonitor = classMonitor; }
    public Integer getMonitorClassId() { return monitorClassId; }
    public void setMonitorClassId(Integer monitorClassId) { this.monitorClassId = monitorClassId; }
    public Integer getRelatedId() { return relatedId; }
    public void setRelatedId(Integer relatedId) { this.relatedId = relatedId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
