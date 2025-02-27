package com.altair288.class_management.repository;

import com.altair288.class_management.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdAndRecipientType(Long recipientId, String recipientType);
    List<Notification> findByCreatedBy(Long teacherId);
}
