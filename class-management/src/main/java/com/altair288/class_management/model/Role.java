package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 统一角色实体（系统登录 + 审批流程）。
 * schema.sql 中列：code, display_name, category, parent_id, level, sort_order, description, enabled
 * 为保持旧代码兼容：保留 getRoleName/setRoleName 方法，内部映射到 displayName。
 */
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;              // 英文/标识代码，如 STUDENT / HOMEROOM

    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;       // 中文显示，如 学生 / 班主任

    @Column(name = "category", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private Category category;        // SYSTEM / APPROVAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Role parent;              // 上级审批角色（仅审批层级用）

    @Column(name = "level", nullable = false)
    private Integer level = 1;        // 层级：根=1

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;    // 同层排序

    @Column(name = "description")
    private String description;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt; // 由数据库默认值生成

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt; // 由数据库 ON UPDATE 维护

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RolePermission> rolePermissions = new HashSet<>();

    public enum Category { SYSTEM, APPROVAL }

    // 新常量（角色代码）
    public static final class Codes {
        public static final String STUDENT = "STUDENT";
        public static final String TEACHER = "TEACHER";
        public static final String PARENT = "PARENT";
        public static final String ADMIN = "ADMIN";
        public static final String HOMEROOM = "HOMEROOM";
        public static final String DEPT_HEAD = "DEPT_HEAD";
        public static final String GRADE_HEAD = "GRADE_HEAD";
        public static final String ACADEMIC_DIRECTOR = "ACADEMIC_DIRECTOR";
        public static final String PRINCIPAL = "PRINCIPAL";
    }

    public Role() {}
    public Role(Integer id) { this.id = id; }

    // 兼容旧代码：roleName 当作 displayName
    public String getRoleName() { return displayName; }
    public void setRoleName(String name) { this.displayName = name; }

    // Getters & Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Role getParent() { return parent; }
    public void setParent(Role parent) { this.parent = parent; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Set<UserRole> getUserRoles() { return userRoles; }
    public void setUserRoles(Set<UserRole> userRoles) { this.userRoles = userRoles; }
    public Set<RolePermission> getRolePermissions() { return rolePermissions; }
    public void setRolePermissions(Set<RolePermission> rolePermissions) { this.rolePermissions = rolePermissions; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}