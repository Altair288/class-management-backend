package com.altair288.class_management.repository;

import com.altair288.class_management.model.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Integer> {
    @Query("select s from ApprovalStep s where s.workflowId = :workflowId and s.enabled = true order by s.stepOrder asc")
    List<ApprovalStep> findEnabledStepsByWorkflow(@Param("workflowId") Integer workflowId);
}
