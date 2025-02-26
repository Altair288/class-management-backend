package com.altair288.class_management.service;

import com.altair288.class_management.model.StudentCredit;
import com.altair288.class_management.repository.StudentCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentCreditService {

    @Autowired
    private StudentCreditRepository studentCreditRepository;

    // 添加学分
    public StudentCredit addCredit(StudentCredit studentCredit) {
        return studentCreditRepository.save(studentCredit);
    }

    // 查询学生的德育学分
    public List<StudentCredit> getStudentCredits(Long studentId) {
        return studentCreditRepository.findByStudentId(studentId);
    }

    // 教师审核学分
    public StudentCredit approveCredit(Long creditId, String status, String report) {
        StudentCredit studentCredit = studentCreditRepository.findById(creditId).orElse(null);
        if (studentCredit != null) {
            studentCredit.setStatus(status);
            studentCredit.setReport(report);
            return studentCreditRepository.save(studentCredit);
        }
        return null;
    }

    // 获取教师审批的学分
    public List<StudentCredit> getCreditsByTeacher(Long teacherId) {
        return studentCreditRepository.findByTeacherId(teacherId);
    }

    // 统计学分
    public int calculateTotalCredit(Long studentId) {
        List<StudentCredit> credits = studentCreditRepository.findByStudentId(studentId);
        return credits.stream().mapToInt(StudentCredit::getCredit).sum();
    }
}
