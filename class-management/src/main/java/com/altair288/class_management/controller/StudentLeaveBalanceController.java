package com.altair288.class_management.controller;

import com.altair288.class_management.model.StudentLeaveBalance;
import com.altair288.class_management.service.StudentLeaveBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave/balance")
@CrossOrigin(origins = "http://localhost:3000")
public class StudentLeaveBalanceController {
    
    @Autowired
    private StudentLeaveBalanceService studentLeaveBalanceService;

    // 辅助方法：解析学年字符串为年份整数
    private Integer parseAcademicYear(String academicYear) {
        if (academicYear == null || academicYear.trim().isEmpty()) {
            return null;
        }
        
        // 尝试解析 "2024-2025" 格式，提取开始年份
        if (academicYear.contains("-")) {
            String[] parts = academicYear.split("-");
            if (parts.length > 0) {
                try {
                    return Integer.parseInt(parts[0].trim());
                } catch (NumberFormatException e) {
                    // 如果解析失败，返回当前年份
                    return 2024;
                }
            }
        }
        
        // 尝试直接解析为整数
        try {
            return Integer.parseInt(academicYear.trim());
        } catch (NumberFormatException e) {
            // 如果解析失败，返回当前年份
            return 2024;
        }
    }

    // 获取学生的请假余额
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<StudentLeaveBalance>> getStudentBalances(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String academicYear) {
        Integer year = parseAcademicYear(academicYear);
        List<StudentLeaveBalance> balances = studentLeaveBalanceService.getStudentBalances(studentId, year);
        return ResponseEntity.ok(balances);
    }

    // 获取特定学生特定类型的余额
    @GetMapping("/student/{studentId}/type/{leaveTypeId}")
    public ResponseEntity<StudentLeaveBalance> getStudentBalance(
            @PathVariable Integer studentId,
            @PathVariable Integer leaveTypeId,
            @RequestParam String academicYear) { // 接口请求参数为academicYear
        Integer year = parseAcademicYear(academicYear);
        StudentLeaveBalance balance = studentLeaveBalanceService.getStudentBalance(studentId, leaveTypeId, year); // 这里传参为year
        if (balance != null) {
            return ResponseEntity.ok(balance);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 获取某个请假类型的所有学生余额
    @GetMapping("/type/{leaveTypeId}")
    public ResponseEntity<List<StudentLeaveBalance>> getBalancesByLeaveType(@PathVariable Integer leaveTypeId) {
        List<StudentLeaveBalance> balances = studentLeaveBalanceService.getBalancesByLeaveType(leaveTypeId);
        return ResponseEntity.ok(balances);
    }

    // 获取某学年的所有余额
    @GetMapping("/year/{academicYear}")
    public ResponseEntity<List<StudentLeaveBalance>> getBalancesByAcademicYear(@PathVariable String academicYear) {
        Integer year = parseAcademicYear(academicYear);
        List<StudentLeaveBalance> balances = studentLeaveBalanceService.getBalancesByYear(year);
        return ResponseEntity.ok(balances);
    }

    // 获取所有余额
    @GetMapping("/all")
    public ResponseEntity<List<StudentLeaveBalance>> getAllBalances() {
        List<StudentLeaveBalance> balances = studentLeaveBalanceService.getAllBalances();
        return ResponseEntity.ok(balances);
    }

    // 创建或更新学生余额
    @PostMapping
    public ResponseEntity<StudentLeaveBalance> saveStudentBalance(@RequestBody StudentLeaveBalance balance) {
        try {
            StudentLeaveBalance saved = studentLeaveBalanceService.saveStudentBalance(balance);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 更新学生余额
    @PutMapping("/{id}")
    public ResponseEntity<StudentLeaveBalance> updateStudentBalance(
            @PathVariable Integer id,
            @RequestBody StudentLeaveBalance balance) {
        balance.setId(id);
        StudentLeaveBalance updated = studentLeaveBalanceService.saveStudentBalance(balance);
        return ResponseEntity.ok(updated);
    }

    // 初始化学生余额
    @PostMapping("/initialize")
    public ResponseEntity<StudentLeaveBalance> initializeStudentBalance(
            @RequestParam Integer studentId,
            @RequestParam Integer leaveTypeId,
            @RequestParam String academicYear,
            @RequestParam Integer totalDays) {
        try {
            Integer year = parseAcademicYear(academicYear);
            StudentLeaveBalance balance = studentLeaveBalanceService.initializeStudentBalance(
                studentId, leaveTypeId, year, totalDays);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 批量初始化学生余额
    @PostMapping("/batch-initialize")
    public ResponseEntity<Void> batchInitializeBalances(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> studentIds = (List<Integer>) request.get("studentIds");
            Integer leaveTypeId = (Integer) request.get("leaveTypeId");
            String academicYear = (String) request.get("academicYear");
            Integer totalDays = (Integer) request.get("totalDays");
            
            Integer year = parseAcademicYear(academicYear);
            studentLeaveBalanceService.batchInitializeBalances(studentIds, leaveTypeId, year, totalDays);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 更新已使用天数
    @PostMapping("/update-used")
    public ResponseEntity<StudentLeaveBalance> updateUsedDays(
            @RequestParam Integer studentId,
            @RequestParam Integer leaveTypeId,
            @RequestParam String academicYear,
            @RequestParam Integer usedDays) {
        try {
            Integer year = parseAcademicYear(academicYear);
            StudentLeaveBalance balance = studentLeaveBalanceService.updateBalance(
                studentId, leaveTypeId, year, usedDays.doubleValue());
            if (balance != null) {
                return ResponseEntity.ok(balance);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 重置余额
    @PostMapping("/reset")
    public ResponseEntity<StudentLeaveBalance> resetBalance(
            @RequestParam Integer studentId,
            @RequestParam Integer leaveTypeId,
            @RequestParam String academicYear) {
        try {
            Integer year = parseAcademicYear(academicYear);
            StudentLeaveBalance balance = studentLeaveBalanceService.resetBalance(
                studentId, leaveTypeId, year);
            if (balance != null) {
                return ResponseEntity.ok(balance);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 为某个学生补齐所有启用类型余额（若已存在则跳过）
    @PostMapping("/student/{studentId}/ensure-all")
    public ResponseEntity<Void> ensureAllBalancesForStudent(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String academicYear) {
        try {
            Integer year = parseAcademicYear(academicYear);
            if (year == null) {
                year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            }
            studentLeaveBalanceService.initializeBalancesForStudentAllEnabled(studentId, year);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
