package com.altair288.class_management.service;

import com.altair288.class_management.model.Teacher;
import com.altair288.class_management.repository.TeacherRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TeacherService {
    @Autowired
    private TeacherRepository teacherRepository;

    public Teacher getTeacherById(Integer id) {
        return teacherRepository.findById(id).orElse(null);
    }
    public Teacher save(Teacher teacher) {
        // Implement the logic to save the teacher entity
        // For example, if using JPA:
        return teacherRepository.save(teacher);
    }

    public List<Teacher> findAll() {
        return teacherRepository.findAll();
    }

    public long count() {
        return teacherRepository.count();
    }

    public Teacher getById(Integer id) {
        return teacherRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("未找到该教师"));
}
}