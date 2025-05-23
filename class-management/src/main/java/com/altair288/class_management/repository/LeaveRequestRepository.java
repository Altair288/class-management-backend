package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByStudentId(Integer studentId);
    List<LeaveRequest> findByTeacherId(Integer teacherId);
}
