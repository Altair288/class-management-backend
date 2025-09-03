package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveTypeWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LeaveTypeWorkflowRepository extends JpaRepository<LeaveTypeWorkflow, Integer> {
    @Query("select ltw from LeaveTypeWorkflow ltw where ltw.leaveTypeId = :leaveTypeId and ltw.enabled = true")
    Optional<LeaveTypeWorkflow> findActiveByLeaveTypeId(@Param("leaveTypeId") Integer leaveTypeId);
}
