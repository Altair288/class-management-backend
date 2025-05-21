package com.altair288.class_management.repository;

import com.altair288.class_management.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class, Integer> {
    Optional<Class> findByName(String name);
}
