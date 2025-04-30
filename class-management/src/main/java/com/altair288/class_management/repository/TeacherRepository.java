package com.altair288.class_management.repository;
import com.altair288.class_management.model.Teacher;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {}