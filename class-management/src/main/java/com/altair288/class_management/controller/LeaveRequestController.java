package com.altair288.class_management.controller;

import com.altair288.class_management.model.LeaveRequest;
import com.altair288.class_management.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    // 学生提交请假申请
    @PostMapping
    public LeaveRequest createLeaveRequest(@RequestBody LeaveRequest leaveRequest) {
        return leaveRequestService.createLeaveRequest(leaveRequest);
    }

    // 查询学生的请假记录
    @GetMapping("/student/{studentId}")
    public List<LeaveRequest> getStudentLeaveRequests(@PathVariable Long studentId) {
        return leaveRequestService.getStudentLeaveRequests(studentId);
    }

    // 教师审批请假申请
    @PostMapping("/approve/{leaveRequestId}")
    public LeaveRequest approveLeaveRequest(@PathVariable Long leaveRequestId,
                                            @RequestParam String status,
                                            @RequestParam Long teacherId) {
        return leaveRequestService.approveLeaveRequest(leaveRequestId, status, teacherId);
    }

    // 教师查看所有请假记录
    @GetMapping("/teacher/{teacherId}")
    public List<LeaveRequest> getLeaveRequestsByTeacher(@PathVariable Long teacherId) {
        return leaveRequestService.getLeaveRequestsByTeacher(teacherId);
    }
}
