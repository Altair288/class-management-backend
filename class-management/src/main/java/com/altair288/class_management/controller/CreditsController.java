package com.altair288.class_management.controller;

import com.altair288.class_management.dto.CreditItemDTO;
import com.altair288.class_management.dto.StudentCreditsDTO;
import com.altair288.class_management.dto.StudentCreditItemDTO;
import com.altair288.class_management.dto.StudentCreditsViewDTO;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.repository.StudentRepository;
import com.altair288.class_management.repository.ClassRepository;
import com.altair288.class_management.service.CreditItemService;
import com.altair288.class_management.service.StudentCreditService;
import com.altair288.class_management.service.StudentEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/credits")
public class CreditsController {
    private final CreditItemService creditItemService;
    private final StudentCreditService studentCreditService;
    private final StudentRepository studentRepository;
    private final StudentEvaluationService evaluationService;
    private final ClassRepository classRepository;

    public CreditsController(CreditItemService creditItemService, StudentCreditService studentCreditService, StudentRepository studentRepository, StudentEvaluationService evaluationService, ClassRepository classRepository) {
        this.creditItemService = creditItemService;
        this.studentCreditService = studentCreditService;
        this.studentRepository = studentRepository;
        this.evaluationService = evaluationService;
        this.classRepository = classRepository;
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

    // 获取某个学生的总分与评级（持久化表）
    @GetMapping("/students/{studentId}/evaluation")
    public ResponseEntity<Map<String,Object>> getStudentEvaluation(@PathVariable Integer studentId) {
        var eval = evaluationService.recomputeForStudent(studentId);
        java.util.Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("studentId", studentId);
        resp.put("total", eval.getTotalScore());
        resp.put("status", eval.getStatus());
        return ResponseEntity.ok(resp);
    }

    // 批量重算：某班级
    @PostMapping("/class/{classId}/evaluation/recompute")
    public ResponseEntity<Map<String,Object>> recomputeClassEvaluation(@PathVariable Integer classId) {
        int n = evaluationService.recomputeForClass(classId);
        java.util.Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("recomputed", n);
        return ResponseEntity.ok(resp);
    }

    // 批量重算：全部学生
    @PostMapping("/evaluation/recompute-all")
    public ResponseEntity<Map<String,Object>> recomputeAllEvaluation() {
        int n = evaluationService.recomputeAll();
        java.util.Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("recomputed", n);
        return ResponseEntity.ok(resp);
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
            var eval = evaluationService.recomputeForStudent(s.getId());
            v.setTotal(eval.getTotalScore());
            v.setStatus(eval.getStatus());
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

    // 将主项目的新规则应用到所有学生
    public static class ApplyRuleRequest { public String mode; }
    @PostMapping("/items/{itemId}/apply")
    public ResponseEntity<Map<String, Object>> applyItemRule(@PathVariable Integer itemId, @RequestBody(required = false) ApplyRuleRequest req) {
        String mode = (req == null) ? null : req.mode;
        int affected = studentCreditService.applyItemRule(itemId, mode);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("affected", affected);
        resp.put("mode", mode == null ? "reset" : mode);
        return ResponseEntity.ok(resp);
    }

    // 统一联合筛选：按关键字(姓名/学号)、班级ID、状态(可同时存在)
    // GET /api/credits/student-union-scores?keyword=&classId=&status=
    @GetMapping("/student-union-scores")
    public ResponseEntity<List<StudentCreditsViewDTO>> unionScores(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) String status
    ) {
        String kw = keyword == null ? null : keyword.trim();
        String st = status == null ? null : status.trim().toLowerCase();
        if (st != null && !st.isEmpty()) {
            if (!("excellent".equals(st) || "good".equals(st) || "warning".equals(st) || "danger".equals(st))) {
                throw new IllegalArgumentException("状态必须为 excellent/good/warning/danger");
            }
        }

        List<Student> base = (classId != null) ? studentRepository.findByClazzId(classId) : studentRepository.findAll();
        if (kw != null && !kw.isEmpty()) {
            String lkw = kw.toLowerCase();
            base = base.stream().filter(s -> {
                String name = s.getName() == null ? "" : s.getName();
                String sno = s.getStudentNo() == null ? "" : s.getStudentNo();
                return name.toLowerCase().contains(lkw) || sno.toLowerCase().contains(lkw);
            }).toList();
        }

        // 批量聚合汇总，避免 N+1
        List<Integer> ids = base.stream().map(Student::getId).toList();
        var projections = studentCreditService.sumByStudentIds(ids);
        java.util.Map<Integer, com.altair288.class_management.repository.StudentCreditRepository.StudentTotalsProjection> sums = new java.util.HashMap<>();
        for (var p : projections) sums.put(p.getStudentId(), p);
        // 状态从 evaluation 表读取（若无可选重算，可依据 total 算出）
        java.util.Map<Integer, String> statuses = evaluationService.getStatusesFor(ids);

        List<StudentCreditsViewDTO> list = new java.util.ArrayList<>();
        for (Student s : base) {
            var agg = sums.get(s.getId());
            if (agg == null) continue; // 没有学分记录则跳过
            String evalStatus = statuses.get(s.getId());
            if (evalStatus == null) {
                double total = agg.getTotal() == null ? 0.0 : agg.getTotal();
                evalStatus = total >= 400 ? "excellent" : total >= 350 ? "good" : total >= 300 ? "warning" : "danger";
            }
            if (st != null && !st.isEmpty() && !st.equals(evalStatus)) continue;

            StudentCreditsViewDTO v = new StudentCreditsViewDTO();
            v.setId(s.getId());
            v.setStudentId(s.getStudentNo());
            v.setStudentName(s.getName());
            v.setClassName(s.getClazz() != null ? s.getClazz().getName() : "");
            v.setDe(nz(agg.getDe()));
            v.setZhi(nz(agg.getZhi()));
            v.setTi(nz(agg.getTi()));
            v.setMei(nz(agg.getMei()));
            v.setLao(nz(agg.getLao()));
            v.setTotal(nz(agg.getTotal()));
            v.setStatus(evalStatus);
            list.add(v);
        }
        return ResponseEntity.ok(list);
    }

    // 仪表盘汇总信息
    // GET /api/credits/dashboard/summary
    @GetMapping("/dashboard/summary")
    public ResponseEntity<Map<String,Object>> dashboardSummary() {
        long totalClasses = classRepository.count();
        long totalStudents = studentRepository.count();

        // 直接一次聚合取各类别总和
        var sums = studentCreditService.sumAllCategories();
        double denom = (totalStudents == 0) ? 1.0 : (double) totalStudents;
        double avgDe = nz(sums.getSumDe()) / denom;
        double avgZhi = nz(sums.getSumZhi()) / denom;
        double avgTi = nz(sums.getSumTi()) / denom;
        double avgMei = nz(sums.getSumMei()) / denom;
        double avgLao = nz(sums.getSumLao()) / denom;
        double avgTotal = (avgDe + avgZhi + avgTi + avgMei + avgLao);

        // 各等级人数用一次 native 聚合
        var buckets = studentCreditService.countBuckets();

        Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("totalStudents", totalStudents);
        resp.put("totalClasses", totalClasses);
        resp.put("countExcellent", buckets.getExc());
        resp.put("countGood", buckets.getGood());
        resp.put("countWarning", buckets.getWarning());
        resp.put("countDanger", buckets.getDanger());
        resp.put("avgDe", avgDe);
        resp.put("avgZhi", avgZhi);
        resp.put("avgTi", avgTi);
        resp.put("avgMei", avgMei);
        resp.put("avgLao", avgLao);
        resp.put("avgTotal", avgTotal);
        return ResponseEntity.ok(resp);
    }

    // 小工具：null as zero
    private static double nz(Double v) { return v == null ? 0.0 : v; }
}
