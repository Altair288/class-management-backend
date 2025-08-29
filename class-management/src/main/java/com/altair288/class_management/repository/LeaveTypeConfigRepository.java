package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveTypeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveTypeConfigRepository extends JpaRepository<LeaveTypeConfig, Integer> {
    List<LeaveTypeConfig> findByEnabledTrue();
    List<LeaveTypeConfig> findByEnabledTrueOrderByTypeCode();
    LeaveTypeConfig findByTypeName(String typeName);
    LeaveTypeConfig findByTypeCode(String typeCode);
}
