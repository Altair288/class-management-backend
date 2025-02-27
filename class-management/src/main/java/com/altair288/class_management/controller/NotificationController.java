package com.altair288.class_management.controller;

import com.altair288.class_management.model.Notification;
import com.altair288.class_management.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // 教师发布通知
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody Notification notification) {
        return ResponseEntity.ok(notificationService.createNotification(notification));
    }

    // 获取学生/家长通知
    @GetMapping("/{recipientId}/{recipientType}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable Long recipientId, @PathVariable String recipientType) {
        return ResponseEntity.ok(notificationService.getNotificationsForRecipient(recipientId, recipientType));
    }

    // 获取教师发布的所有通知
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<Notification>> getNotificationsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(notificationService.getNotificationsByTeacher(teacherId));
    }

    // 标记通知为已读
    @PutMapping("/mark-read/{notificationId}")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId));
    }
}
