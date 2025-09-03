package com.altair288.class_management.repository;

import com.altair288.class_management.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByCode(String code);
    Optional<Department> findByName(String name);
}
