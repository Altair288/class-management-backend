package com.altair288.class_management.MessageCenter.repository;

import com.altair288.class_management.MessageCenter.model.NotificationRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    List<NotificationRecipient> findByUserIdAndReadStatusFalseOrderByIdDesc(Integer userId);
    long countByUserIdAndReadStatusFalse(Integer userId);

    // 历史/分页查询：readStatus 可为空表示全部
    @org.springframework.data.jpa.repository.Query("SELECT r FROM NotificationRecipient r WHERE r.userId = :userId AND (:readStatus IS NULL OR r.readStatus = :readStatus) ORDER BY r.id DESC")
    org.springframework.data.domain.Page<NotificationRecipient> findHistory(
            @org.springframework.data.repository.query.Param("userId") Integer userId,
            @org.springframework.data.repository.query.Param("readStatus") Boolean readStatus,
            org.springframework.data.domain.Pageable pageable
    );
}
