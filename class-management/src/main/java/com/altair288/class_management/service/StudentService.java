package com.altair288.class_management.service;

import com.altair288.class_management.model.Student;
import com.altair288.class_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public Student getStudentById(Integer id) {
        return studentRepository.findById(id).orElse(null);
    }

    public Student getStudentByStudentNo(String studentNo) {
        return studentRepository.findByStudentNo(studentNo)
            .orElseThrow(() -> new IllegalArgumentException("未找到该学号对应的学生"));
    }
    public Student save(Student student) {
    // Implement the logic to save the teacher entity
    // For example, if using JPA:
    return studentRepository.save(student);
    }

    public Long count() {
        return studentRepository.count();
    }

    public Long countByClassId(Integer classId) {
        return studentRepository.countByClazzId(classId);
    }
}
