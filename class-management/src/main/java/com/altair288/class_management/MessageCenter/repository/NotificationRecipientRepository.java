package com.altair288.class_management.MessageCenter.repository;

import com.altair288.class_management.MessageCenter.model.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    List<NotificationRecipient> findByUserIdAndReadStatusFalseOrderByIdDesc(Integer userId);
    long countByUserIdAndReadStatusFalse(Integer userId);
}
