package com.altair288.class_management.service;

import com.altair288.class_management.model.LeaveRequest;
import com.altair288.class_management.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // 学生提交请假申请
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        return leaveRequestRepository.save(leaveRequest);
    }

    // 查询学生的请假记录
    public List<LeaveRequest> getStudentLeaveRequests(Long studentId) {
        return leaveRequestRepository.findByStudentId(studentId);
    }

    // 教师审批请假申请
    public LeaveRequest approveLeaveRequest(Long leaveRequestId, String status, Long teacherId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId).orElse(null);
        if (leaveRequest != null) {
            leaveRequest.setStatus(status);
            leaveRequest.setTeacherId(teacherId);
            leaveRequest.setApprovalDate(new java.sql.Date(System.currentTimeMillis()));
            return leaveRequestRepository.save(leaveRequest);
        }
        return null;
    }

    // 教师查看所有请假记录
    public List<LeaveRequest> getLeaveRequestsByTeacher(Long teacherId) {
        return leaveRequestRepository.findByTeacherId(teacherId);
    }
}
