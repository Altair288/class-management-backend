package com.altair288.class_management.dto;

import com.altair288.class_management.model.User.UserType;

public class UserDTO {
    private Integer id;
    private String username;
    private UserType userType;
    // 是否为班长（当前用户拥有 CLASS_MONITOR 角色）
    private Boolean classMonitor;
    // 若是班长，其所负责的班级ID（基于其自身 student.clazz.id）
    private Integer monitorClassId;

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
    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    public Boolean getClassMonitor() { return classMonitor; }
    public void setClassMonitor(Boolean classMonitor) { this.classMonitor = classMonitor; }
    public Integer getMonitorClassId() { return monitorClassId; }
    public void setMonitorClassId(Integer monitorClassId) { this.monitorClassId = monitorClassId; }
}
