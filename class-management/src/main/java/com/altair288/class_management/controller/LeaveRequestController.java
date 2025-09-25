package com.altair288.class_management.controller;

import com.altair288.class_management.model.LeaveRequest;
import com.altair288.class_management.model.LeaveTypeConfig;
import com.altair288.class_management.dto.CurrentUserLeaveInfoDTO;
import com.altair288.class_management.dto.LeaveCalendarDTO;
import org.springframework.format.annotation.DateTimeFormat;
import com.altair288.class_management.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000", "http://192.168.*:*", "http://172.*:*", "http://10.*:*"}, allowCredentials = "true")
public class LeaveRequestController {
    
    @Autowired
    private LeaveRequestService leaveRequestService;

    // 提交请假申请
    @PostMapping("/request")
    public ResponseEntity<LeaveRequest> submitLeaveRequest(@RequestBody LeaveRequest leaveRequest) {
        try {
            LeaveRequest savedRequest = leaveRequestService.submitLeaveRequest(leaveRequest);
            return ResponseEntity.ok(savedRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取学生的请假申请
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> getLeaveRequestsByStudent(@PathVariable Integer studentId) {
        return ResponseEntity.ok(leaveRequestService.getByStudentAsDTO(studentId));
    }

    // 获取教师负责的请假申请
    @GetMapping("/teacher/{teacherId}") // 教师用户身份接口
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> getLeaveRequestsByTeacher(@PathVariable Integer teacherId) {
        return ResponseEntity.ok(leaveRequestService.getByTeacherAsDTO(teacherId));
    }

    // 获取教师当前待处理(待审批)的请假申请（推荐教师端使用）
    @GetMapping("/teacher/{teacherId}/pending")
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> getPendingLeaveRequestsByTeacher(@PathVariable Integer teacherId) {
        return ResponseEntity.ok(leaveRequestService.getPendingByTeacherAsDTO(teacherId));
    }

    // 获取所有请假申请
    @GetMapping("/all") // 管理员用户身份接口
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> getAllLeaveRequests() {
        return ResponseEntity.ok(leaveRequestService.getAllAsDTO());
    }

    // 根据状态获取请假申请
    @GetMapping("/status/{status}")
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> getLeaveRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(leaveRequestService.getByStatusAsDTO(status));
    }

    // 根据日期范围获取请假申请
    @GetMapping("/date-range")
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> getLeaveRequestsByDateRange(
            @RequestParam Date startDate, 
            @RequestParam Date endDate) {
        return ResponseEntity.ok(leaveRequestService.getByDateRangeAsDTO(startDate, endDate));
    }

    // 获取单个请假申请详情
    @GetMapping("/{id}")
    public ResponseEntity<com.altair288.class_management.dto.LeaveRequestListDTO> getLeaveRequestById(@PathVariable Integer id) {
        var dto = leaveRequestService.getOneAsDTO(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    // 更明确的 detail 路径（前端可渐进迁移使用）
    @GetMapping("/{id}/detail")
    public ResponseEntity<com.altair288.class_management.dto.LeaveRequestListDTO> getLeaveRequestDetail(@PathVariable Integer id) {
        var dto = leaveRequestService.getOneAsDTO(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    // 批准请假申请
    @PostMapping("/{id}/approve")
    public ResponseEntity<com.altair288.class_management.dto.LeaveRequestListDTO> approveLeaveRequest(
            @PathVariable Integer id,
            @RequestParam Integer approverId,
            @RequestParam(required = false) String comments) {
        try {
            var dto = leaveRequestService.approveLeaveRequestAsDTO(id, approverId, comments);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 拒绝请假申请
    @PostMapping("/{id}/reject")
    public ResponseEntity<com.altair288.class_management.dto.LeaveRequestListDTO> rejectLeaveRequest(
            @PathVariable Integer id,
            @RequestParam Integer approverId,
            @RequestParam(required = false) String comments) {
        try {
            var dto = leaveRequestService.rejectLeaveRequestAsDTO(id, approverId, comments);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取请假统计信息
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLeaveStatistics() {
        Map<String, Object> stats = leaveRequestService.getLeaveStatistics();
        return ResponseEntity.ok(stats);
    }

    // 获取当前登录用户信息，用于自动填充申请单
    @GetMapping("/current-user-info")
    public ResponseEntity<CurrentUserLeaveInfoDTO> getCurrentUserLeaveInfo() {
        CurrentUserLeaveInfoDTO dto = leaveRequestService.getCurrentUserLeaveInfo();
        return ResponseEntity.ok(dto);
    }

    // 获取请假类型配置
    @GetMapping("/types")
    public ResponseEntity<List<LeaveTypeConfig>> getLeaveTypes() {
        List<LeaveTypeConfig> types = leaveRequestService.getLeaveTypes();
        return ResponseEntity.ok(types);
    }

    // 获取学生请假余额 - 已移至 StudentLeaveBalanceController
    // @GetMapping("/balance/student/{studentId}")
    // public ResponseEntity<List<StudentLeaveBalance>> getStudentLeaveBalances(
    //         @PathVariable Integer studentId,
    //         @RequestParam(required = false) String academicYear) {
    //     List<StudentLeaveBalance> balances = leaveRequestService.getStudentLeaveBalances(studentId, academicYear);
    //     return ResponseEntity.ok(balances);
    // }

    // 更新请假申请
    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> updateLeaveRequest(
            @PathVariable Integer id, 
            @RequestBody LeaveRequest leaveRequest) {
        LeaveRequest existing = leaveRequestService.getById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        leaveRequest.setId(id);
        LeaveRequest updated = leaveRequestService.save(leaveRequest);
        return ResponseEntity.ok(updated);
    }

    // 删除请假申请
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Integer id) {
        LeaveRequest existing = leaveRequestService.getById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 只允许删除待审批状态的申请
        if (!"待审批".equals(existing.getStatus())) {
            return ResponseEntity.badRequest().build();
        }
        
    // 这里应该实现删除逻辑，但为了数据完整性，建议只是更改状态而不是真正删除
    existing.setStatus("已撤销");
        leaveRequestService.save(existing);
        
        return ResponseEntity.ok().build();
    }

    // 兼容旧版本API - 保持向后兼容
    @PostMapping("/submit")
    public ResponseEntity<LeaveRequest> submitLeave(@RequestBody LeaveRequest leaveRequest) {
        return submitLeaveRequest(leaveRequest);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<com.altair288.class_management.dto.LeaveRequestListDTO> approveLeave(@PathVariable Integer id, @RequestParam boolean approved) {
        if (approved) {
            return approveLeaveRequest(id, 1, "系统自动批准");
        } else {
            return rejectLeaveRequest(id, 1, "系统自动拒绝");
        }
    }

    // 批量批准
    @PostMapping("/batch/approve")
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> batchApprove(
            @RequestParam Integer approverId,
            @RequestBody BatchActionRequest body) {
        var list = leaveRequestService.batchApprove(body.getIds(), approverId, body.getComments());
        return ResponseEntity.ok(list);
    }

    // 批量拒绝
    @PostMapping("/batch/reject")
    public ResponseEntity<List<com.altair288.class_management.dto.LeaveRequestListDTO>> batchReject(
            @RequestParam Integer approverId,
            @RequestBody BatchActionRequest body) {
        var list = leaveRequestService.batchReject(body.getIds(), approverId, body.getComments());
        return ResponseEntity.ok(list);
    }

    // 内部静态请求体类
    public static class BatchActionRequest {
        private java.util.List<Integer> ids;
        private String comments;
        public java.util.List<Integer> getIds() { return ids; }
        public void setIds(java.util.List<Integer> ids) { this.ids = ids; }
        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    // 日历视图：批量返回轻量字段，避免 N+1
    // GET /api/leave/calendar?classId=&status=&start=&end=
    @GetMapping("/calendar")
    public ResponseEntity<List<LeaveCalendarDTO>> calendar(
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end) {
        List<LeaveCalendarDTO> list = leaveRequestService.getCalendarData(classId, status, start, end);
        return ResponseEntity.ok(list);
    }

    // 学生个人日历：仅返回该学生的请假记录（可按状态和日期范围过滤）
    // GET /api/leave/calendar/student/{studentId}?status=&start=&end=
    @GetMapping("/calendar/student/{studentId}")
    public ResponseEntity<List<LeaveCalendarDTO>> studentCalendar(
            @PathVariable Integer studentId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date end) {
        List<LeaveCalendarDTO> list = leaveRequestService.getStudentCalendarData(studentId, status, start, end);
        return ResponseEntity.ok(list);
    }
}
