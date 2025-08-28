package com.altair288.class_management.service;

import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.StudentEvaluation;
import com.altair288.class_management.repository.StudentEvaluationRepository;
import com.altair288.class_management.repository.StudentRepository;
import com.altair288.class_management.repository.StudentCreditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class StudentEvaluationService {
    private final StudentRepository studentRepository;
    private final StudentEvaluationRepository evaluationRepository;
    private final StudentCreditRepository studentCreditRepository;

    public StudentEvaluationService(StudentRepository studentRepository,
                                    StudentEvaluationRepository evaluationRepository,
                                    StudentCreditRepository studentCreditRepository) {
        this.studentRepository = studentRepository;
        this.evaluationRepository = evaluationRepository;
        this.studentCreditRepository = studentCreditRepository;
    }

    private String statusOf(double total) {
        return total >= 400 ? "excellent" : total >= 350 ? "good" : total >= 300 ? "warning" : "danger";
    }

    @Transactional
    public StudentEvaluation recomputeForStudent(Integer studentId) {
        Student s = studentRepository.findById(studentId).orElseThrow(() -> new IllegalArgumentException("学生不存在"));
        double de = nz(studentCreditRepository.sumScoreByStudentAndCategory(studentId, "德"));
        double zhi = nz(studentCreditRepository.sumScoreByStudentAndCategory(studentId, "智"));
        double ti = nz(studentCreditRepository.sumScoreByStudentAndCategory(studentId, "体"));
        double mei = nz(studentCreditRepository.sumScoreByStudentAndCategory(studentId, "美"));
        double lao = nz(studentCreditRepository.sumScoreByStudentAndCategory(studentId, "劳"));
        double total = de + zhi + ti + mei + lao;
        String status = statusOf(total);
        StudentEvaluation eval = evaluationRepository.findByStudent_Id(studentId).orElseGet(StudentEvaluation::new);
        eval.setStudent(s);
        eval.setTotalScore(total);
        eval.setStatus(status);
        eval.setUpdatedAt(new Date());
        return evaluationRepository.save(eval);
    }

    private double nz(Double v) { return v == null ? 0.0 : v; }

    @Transactional
    public int recomputeForClass(Integer classId) {
        var students = studentRepository.findByClazzId(classId);
        int n = 0;
        for (Student s : students) {
            recomputeForStudent(s.getId());
            n++;
        }
        return n;
    }

    @Transactional
    public int recomputeAll() {
        var students = studentRepository.findAll();
        int n = 0;
        for (Student s : students) {
            recomputeForStudent(s.getId());
            n++;
        }
        return n;
    }

    @Transactional(readOnly = true)
    public java.util.Map<Integer, String> getStatusesFor(java.util.Collection<Integer> studentIds) {
        java.util.Map<Integer, String> map = new java.util.HashMap<>();
        for (Integer id : studentIds) {
            var opt = evaluationRepository.findByStudent_Id(id);
            opt.ifPresent(ev -> map.put(id, ev.getStatus()));
        }
        return map;
    }
}
