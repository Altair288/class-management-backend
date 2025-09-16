package com.altair288.class_management.MessageCenter.repository;

import com.altair288.class_management.MessageCenter.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByDedupeKey(String dedupeKey);
}
