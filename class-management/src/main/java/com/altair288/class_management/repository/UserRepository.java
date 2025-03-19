// src/main/java/com/altair288/class_management/repository/UserRepository.java
package com.altair288.class_management.repository;

import com.altair288.class_management.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
    boolean existsByUsername(String username);
}