package com.altair288.class_management.repository;

import com.altair288.class_management.model.StudentCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentCreditRepository extends JpaRepository<StudentCredit, Long> {
    List<StudentCredit> findByStudentId(Long studentId);
    List<StudentCredit> findByTeacherId(Long teacherId);
}
