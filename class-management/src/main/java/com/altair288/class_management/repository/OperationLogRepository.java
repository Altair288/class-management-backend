package com.altair288.class_management.repository;

import com.altair288.class_management.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationLogRepository extends JpaRepository<OperationLog, Integer> {}
