package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveAttachmentRepository extends JpaRepository<LeaveAttachment, Integer> {
    List<LeaveAttachment> findByLeaveRequestId(Integer leaveRequestId);
    List<LeaveAttachment> findByCreatedBy(Integer createdBy);
}
