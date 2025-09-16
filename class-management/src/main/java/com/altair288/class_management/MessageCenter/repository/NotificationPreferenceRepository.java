package com.altair288.class_management.MessageCenter.repository;

import com.altair288.class_management.MessageCenter.model.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    Optional<NotificationPreference> findByUserIdAndNotificationTypeAndChannel(Integer userId, String notificationType, String channel);
    List<NotificationPreference> findByUserId(Integer userId);
}
