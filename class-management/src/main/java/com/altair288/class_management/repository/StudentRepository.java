package com.altair288.class_management.repository;

import com.altair288.class_management.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentNo(String studentNo);
}
