package com.altair288.class_management.repository;

import com.altair288.class_management.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Integer> {
	Optional<Parent> findByPhone(String phone);
	Optional<Parent> findByEmail(String email);
}