package com.altair288.class_management.repository;

import com.altair288.class_management.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByCode(String code);
    List<Role> findByCategoryOrderByLevelAscSortOrderAsc(Role.Category category);
}