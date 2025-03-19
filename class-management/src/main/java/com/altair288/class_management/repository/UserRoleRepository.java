package com.altair288.class_management.repository;

import com.altair288.class_management.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    List<UserRole> findByUserId(Integer userId);
    List<UserRole> findByRoleId(Integer roleId);
}