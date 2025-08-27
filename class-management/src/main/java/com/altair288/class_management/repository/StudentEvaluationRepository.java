package com.altair288.class_management.repository;

import com.altair288.class_management.model.StudentEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentEvaluationRepository extends JpaRepository<StudentEvaluation, Integer> {
    Optional<StudentEvaluation> findByStudent_Id(Integer studentId);
}
