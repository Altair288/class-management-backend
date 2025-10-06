package com.altair288.class_management.repository;

import com.altair288.class_management.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;


public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentNo(String studentNo);
    Optional<Student> findByPhone(String phone);
    Optional<Student> findByEmail(String email);
    List<Student> findByName(String name);
    long countByClazzId(Integer classId);
    List<Student> findByClazzId(Integer clazzId);
}
