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

    // 学生提交请假
    @PostMapping
    public LeaveRequest submitLeave(@RequestBody LeaveRequest leaveRequest) {
        return leaveRequestService.submitLeaveRequest(leaveRequest);
    }

    // 学生查自己的请假
    @GetMapping("/student/{studentId}")
    public List<LeaveRequest> getStudentLeaves(@PathVariable Integer studentId) {
        return leaveRequestService.getLeaveRequestsByStudent(studentId);
    }

    // 教师查自己审批的请假
    @GetMapping("/teacher/{teacherId}")
    public List<LeaveRequest> getTeacherLeaves(@PathVariable Integer teacherId) {
        return leaveRequestService.getLeaveRequestsByTeacher(teacherId);
    }

    // 教师审批
    @PutMapping("/{id}/approve")
    public LeaveRequest approveLeave(@PathVariable Integer id, @RequestParam boolean approved) {
        LeaveRequest leaveRequest = leaveRequestService.getById(id);
        leaveRequest.setStatus(approved ? "已批准" : "已拒绝");
        leaveRequest.setReviewedAt(new java.util.Date());
        return leaveRequestService.save(leaveRequest);
    }

    // 管理员/教师查所有请假
    @GetMapping
    public List<LeaveRequest> getAll() {
        return leaveRequestService.getAll();
    }
}
