package com.altair288.class_management.controller;

import com.altair288.class_management.model.LeaveRequest;
import com.altair288.class_management.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    // 学生提交请假申请
    @PostMapping
    public ResponseEntity<LeaveRequest> createLeaveRequest(@RequestBody LeaveRequest leaveRequest) {
        return ResponseEntity.ok(leaveRequestService.createLeaveRequest(leaveRequest));
    }

    // 查询学生的请假记录
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<LeaveRequest>> getStudentLeaveRequests(@PathVariable Long studentId) {
        return ResponseEntity.ok(leaveRequestService.getStudentLeaveRequests(studentId));
    }

    // 教师审批请假
    @PutMapping("/approve/{leaveRequestId}")
    public ResponseEntity<LeaveRequest> approveLeaveRequest(@PathVariable Long leaveRequestId,
                                                            @RequestBody Map<String, String> requestBody) {
        String status = requestBody.get("status");
        Long teacherId = Long.parseLong(requestBody.get("teacherId"));
        LeaveRequest leaveRequest = leaveRequestService.approveLeaveRequest(leaveRequestId, status, teacherId);
        return ResponseEntity.ok(leaveRequest);
    }

    // 教师查看自己的审批记录
    @GetMapping("/teacher/{teacherId}") 
    public ResponseEntity<List<LeaveRequest>> getLeaveRequestsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(leaveRequestService.getLeaveRequestsByTeacher(teacherId));
    }
}
