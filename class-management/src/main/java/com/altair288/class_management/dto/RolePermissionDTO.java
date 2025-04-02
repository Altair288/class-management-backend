package com.altair288.class_management.dto;

public class RolePermissionDTO {
    private Integer id;
    private Integer roleId;
    private String roleName;
    private Integer permissionId;
    private String permissionName;
    private Integer grantedByUserId;

    // 构造函数
    public RolePermissionDTO(Integer id, Integer roleId, String roleName, Integer permissionId, String permissionName, Integer grantedByUserId) {
        this.id = id;
        this.roleId = roleId;
        this.roleName = roleName;
        this.permissionId = permissionId;
        this.permissionName = permissionName;
        this.grantedByUserId = grantedByUserId;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getRoleId() { return roleId; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Integer getPermissionId() { return permissionId; }
    public void setPermissionId(Integer permissionId) { this.permissionId = permissionId; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public Integer getGrantedByUserId() { return grantedByUserId; }
    public void setGrantedByUserId(Integer grantedByUserId) { this.grantedByUserId = grantedByUserId; }
}
