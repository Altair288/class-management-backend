package com.altair288.class_management.service;

import com.altair288.class_management.model.LeaveRequest;
import com.altair288.class_management.repository.LeaveRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRequestService {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    // 学生提交请假申请
    public LeaveRequest createLeaveRequest(LeaveRequest leaveRequest) {
        leaveRequest.setStatus("待审批");
        return leaveRequestRepository.save(leaveRequest);
    }

    // 查询某个学生的所有请假申请
    public List<LeaveRequest> getStudentLeaveRequests(Long studentId) {
        return leaveRequestRepository.findByStudentId(studentId);
    }

    // 教师审批请假
    public LeaveRequest approveLeaveRequest(Long leaveRequestId, String status, Long teacherId) {
        Optional<LeaveRequest> optionalLeaveRequest = leaveRequestRepository.findById(leaveRequestId);
        if (optionalLeaveRequest.isPresent()) {
            LeaveRequest leaveRequest = optionalLeaveRequest.get();
            leaveRequest.setStatus(status);
            leaveRequest.setTeacherId(teacherId);
            leaveRequest.setApprovalDate(new Date());
            return leaveRequestRepository.save(leaveRequest);
        }
        return null;
    }

    // 教师查看自己审批的请假记录
    public List<LeaveRequest> getLeaveRequestsByTeacher(Long teacherId) {
        return leaveRequestRepository.findByTeacherId(teacherId);
    }
}
