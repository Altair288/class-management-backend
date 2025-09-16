package com.altair288.class_management.MessageCenter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_preference", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_type_channel", columnNames = { "user_id", "notification_type", "channel" })
}, indexes = { @Index(name = "idx_user_channel", columnList = "user_id, channel") })
public class NotificationPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "notification_type", length = 50, nullable = false)
    private String notificationType; // 与 NotificationType.name 对应

    @Column(name = "channel", length = 20, nullable = false)
    private String channel; // INBOX / EMAIL / SMS / WEBHOOK

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Getters
    public Long getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getChannel() {
        return channel;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
