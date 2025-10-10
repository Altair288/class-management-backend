package com.altair288.class_management.repository;
import com.altair288.class_management.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
	Optional<Teacher> findByPhone(String phone);
	Optional<Teacher> findByEmail(String email);
}