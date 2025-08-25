package com.altair288.class_management.controller;

import com.altair288.class_management.dto.CreditItemDTO;
import com.altair288.class_management.dto.StudentCreditsDTO;
import com.altair288.class_management.dto.StudentCreditItemDTO;
import com.altair288.class_management.dto.StudentCreditsViewDTO;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.repository.StudentRepository;
import com.altair288.class_management.service.CreditItemService;
import com.altair288.class_management.service.StudentCreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credits")
public class CreditsController {
    private final CreditItemService creditItemService;
    private final StudentCreditService studentCreditService;
    private final StudentRepository studentRepository;

    public CreditsController(CreditItemService creditItemService, StudentCreditService studentCreditService, StudentRepository studentRepository) {
        this.creditItemService = creditItemService;
        this.studentCreditService = studentCreditService;
        this.studentRepository = studentRepository;
    }

    // 列表（支持可选 category 过滤）
    @GetMapping("/items")
    public ResponseEntity<List<CreditItemDTO>> listItems(@RequestParam(required = false) String category) {
        return ResponseEntity.ok(creditItemService.list(category));
    }

    // 新增（与你前端 POST /api/credits/items 对齐）
    @PostMapping("/items")
    public ResponseEntity<CreditItemDTO> create(@RequestBody CreditItemDTO dto) {
        return ResponseEntity.ok(creditItemService.create(dto));
    }

    // 编辑（与你前端 POST /api/credits/items/{id} 对齐）
    @PostMapping("/items/{id}")
    public ResponseEntity<CreditItemDTO> update(@PathVariable Integer id, @RequestBody CreditItemDTO dto) {
        return ResponseEntity.ok(creditItemService.update(id, dto));
    }

    // 获取某个学生的总分（五项）
    @GetMapping("/students/{studentId}/totals")
    public ResponseEntity<StudentCreditsDTO> getStudentTotals(@PathVariable Integer studentId) {
        return ResponseEntity.ok(studentCreditService.getTotalsForStudent(studentId));
    }

    // 获取某个班级下所有学生的总分
    @GetMapping("/class/{classId}/students")
    public ResponseEntity<List<StudentCreditsViewDTO>> getClassStudentsTotals(@PathVariable Integer classId) {
        // 组装为前端需要的结构：id/studentId/studentName/className/德智体美劳/total/status
        List<Student> students = studentRepository.findByClazzId(classId);
        List<StudentCreditsViewDTO> list = new java.util.ArrayList<>();
        for (Student s : students) {
            var totalDto = studentCreditService.getTotalsForStudent(s.getId());
            StudentCreditsViewDTO v = new StudentCreditsViewDTO();
            v.setId(s.getId());
            v.setStudentId(s.getStudentNo());
            v.setStudentName(s.getName());
            v.setClassName(s.getClazz() != null ? s.getClazz().getName() : "");
            v.setDe(totalDto.getDe());
            v.setZhi(totalDto.getZhi());
            v.setTi(totalDto.getTi());
            v.setMei(totalDto.getMei());
            v.setLao(totalDto.getLao());
            double total = totalDto.getDe() + totalDto.getZhi() + totalDto.getTi() + totalDto.getMei() + totalDto.getLao();
            v.setTotal(total);
            // 映射等级：>=400 优秀; >=350 良好; >=300 预警; 否则 危险
            String status = total >= 400 ? "excellent" : total >= 350 ? "good" : total >= 300 ? "warning" : "danger";
            v.setStatus(status);
            list.add(v);
        }
        return ResponseEntity.ok(list);
    }

    // 学生维度：按类别列出所有项目与当前分值（category 可选，空则全部）
    @GetMapping("/students/{studentId}/items")
    public ResponseEntity<List<StudentCreditItemDTO>> listStudentItems(@PathVariable Integer studentId, @RequestParam(required = false) String category) {
        return ResponseEntity.ok(studentCreditService.listStudentItems(studentId, category));
    }

    // 学生维度：对某项目增减分（delta 正负皆可）
    public static class UpdateScoreRequest { public Integer creditItemId; public Double delta; }
    @PostMapping("/students/{studentId}/update-score")
    public ResponseEntity<String> updateStudentScore(@PathVariable Integer studentId, @RequestBody UpdateScoreRequest req) {
        studentCreditService.updateScore(studentId, req.creditItemId, req.delta);
        return ResponseEntity.ok("OK");
    }

    // 学生维度：设置某项目的绝对分值
    public static class SetScoreRequest { public Integer creditItemId; public Double value; }
    @PostMapping("/students/{studentId}/set-score")
    public ResponseEntity<String> setStudentScore(@PathVariable Integer studentId, @RequestBody SetScoreRequest req) {
        studentCreditService.setScore(studentId, req.creditItemId, req.value);
        return ResponseEntity.ok("OK");
    }
}
