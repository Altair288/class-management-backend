package com.altair288.class_management.controller;

import com.altair288.class_management.model.StudentCredit;
import com.altair288.class_management.service.StudentCreditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
public class StudentCreditController {

    @Autowired
    private StudentCreditService studentCreditService;

    // 添加学分
    @PostMapping
    public StudentCredit addCredit(@RequestBody StudentCredit studentCredit) {
        return studentCreditService.addCredit(studentCredit);
    }

    // 查询学生的德育学分
    @GetMapping("/student/{studentId}")
    public List<StudentCredit> getStudentCredits(@PathVariable Long studentId) {
        return studentCreditService.getStudentCredits(studentId);
    }

    // 教师审核学分
    @PostMapping("/approve/{creditId}")
    public StudentCredit approveCredit(@PathVariable Long creditId, @RequestParam String status, @RequestParam String report) {
        return studentCreditService.approveCredit(creditId, status, report);
    }

    // 统计学生学分
    @GetMapping("/total/{studentId}")
    public int calculateTotalCredit(@PathVariable Long studentId) {
        return studentCreditService.calculateTotalCredit(studentId);
    }

    // 获取教师审批的学分
    @GetMapping("/teacher/{teacherId}")
    public List<StudentCredit> getCreditsByTeacher(@PathVariable Long teacherId) {
        return studentCreditService.getCreditsByTeacher(teacherId);
    }
}
