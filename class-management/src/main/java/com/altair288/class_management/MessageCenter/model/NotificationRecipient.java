package com.altair288.class_management.MessageCenter.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification_recipient", uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_user", columnNames = { "notification_id", "user_id" })
}, indexes = {
                @Index(name = "idx_notification", columnList = "notification_id"),
                @Index(name = "idx_user_unread", columnList = "user_id, read_status")
})
public class NotificationRecipient {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "notification_id", nullable = false, foreignKey = @ForeignKey(name = "fk_nr_notification"))
        private Notification notification;

        @Column(name = "user_id", nullable = false)
        private Integer userId;

        @Column(name = "inbox_enabled", nullable = false)
        private Boolean inboxEnabled = true;

        @Column(name = "email_enabled", nullable = false)
        private Boolean emailEnabled = false;

        @Column(name = "email_sent", nullable = false)
        private Boolean emailSent = false;

        @Column(name = "read_status", nullable = false)
        private Boolean readStatus = false;

        @Column(name = "read_at")
        private Instant readAt;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private Instant createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private Instant updatedAt;

        // Getters & Setters
        public Long getId() {
                return id;
        }

        public Notification getNotification() {
                return notification;
        }

        public void setNotification(Notification notification) {
                this.notification = notification;
        }

        public Integer getUserId() {
                return userId;
        }

        public void setUserId(Integer userId) {
                this.userId = userId;
        }

        public Boolean getInboxEnabled() {
                return inboxEnabled;
        }

        public void setInboxEnabled(Boolean inboxEnabled) {
                this.inboxEnabled = inboxEnabled;
        }

        public Boolean getEmailEnabled() {
                return emailEnabled;
        }

        public void setEmailEnabled(Boolean emailEnabled) {
                this.emailEnabled = emailEnabled;
        }

        public Boolean getEmailSent() {
                return emailSent;
        }

        public void setEmailSent(Boolean emailSent) {
                this.emailSent = emailSent;
        }

        public Boolean getReadStatus() {
                return readStatus;
        }

        public void setReadStatus(Boolean readStatus) {
                this.readStatus = readStatus;
        }

        public Instant getReadAt() {
                return readAt;
        }

        public void setReadAt(Instant readAt) {
                this.readAt = readAt;
        }

        public Instant getCreatedAt() {
                return createdAt;
        }

        public Instant getUpdatedAt() {
                return updatedAt;
        }
}
