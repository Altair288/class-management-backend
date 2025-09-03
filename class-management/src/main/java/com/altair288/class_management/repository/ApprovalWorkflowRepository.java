package com.altair288.class_management.repository;

import com.altair288.class_management.model.ApprovalWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ApprovalWorkflowRepository extends JpaRepository<ApprovalWorkflow, Integer> {
    @Query("select w from ApprovalWorkflow w where w.workflowCode = :code")
    Optional<ApprovalWorkflow> findByCode(@Param("code") String code);
}
