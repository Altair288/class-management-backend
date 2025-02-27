package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStudentId(Long studentId);  // 查询学生的请假记录
    List<LeaveRequest> findByTeacherId(Long teacherId);  // 查询教师审批的所有请假记录
}
