// src/main/java/com/altair288/class_management/repository/RolePermissionRepository.java
package com.altair288.class_management.repository;

import com.altair288.class_management.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    List<RolePermission> findByRoleId(Integer roleId);
    List<RolePermission> findByPermissionId(Integer permissionId);
}