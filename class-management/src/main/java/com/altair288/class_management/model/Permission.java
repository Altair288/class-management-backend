package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "permission")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "permission_name", unique = true, nullable = false)
    private String permissionName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL)
    private Set<RolePermission> rolePermissions;

    public Permission(Integer id) {
        this.id = id;
    }
        
    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPermissionName() { return permissionName; }
    public void setPermissionName(String permissionName) { this.permissionName = permissionName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<RolePermission> getRolePermissions() { return rolePermissions; }
    public void setRolePermissions(Set<RolePermission> rolePermissions) { this.rolePermissions = rolePermissions; }
}