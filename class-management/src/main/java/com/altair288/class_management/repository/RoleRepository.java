package com.altair288.class_management.repository;

import com.altair288.class_management.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName);  // 根据中文角色名查询
}