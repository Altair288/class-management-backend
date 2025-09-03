package com.altair288.class_management.repository;

import com.altair288.class_management.model.ClassLeaveTypeWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ClassLeaveTypeWorkflowRepository extends JpaRepository<ClassLeaveTypeWorkflow, Integer> {
    @Query("select c from ClassLeaveTypeWorkflow c where c.classId = :classId and c.leaveTypeId = :leaveTypeId and c.enabled = true")
    Optional<ClassLeaveTypeWorkflow> findActiveByClassAndType(@Param("classId") Integer classId, @Param("leaveTypeId") Integer leaveTypeId);
}
