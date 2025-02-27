package com.altair288.class_management.service;

import com.altair288.class_management.model.Notification;
import com.altair288.class_management.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // 发布通知
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    // 获取某个学生/家长的通知
    public List<Notification> getNotificationsForRecipient(Long recipientId, String recipientType) {
        return notificationRepository.findByRecipientIdAndRecipientType(recipientId, recipientType);
    }

    // 获取教师发布的通知
    public List<Notification> getNotificationsByTeacher(Long teacherId) {
        return notificationRepository.findByCreatedBy(teacherId);
    }

    // 标记通知为已读
    public Notification markAsRead(Long notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isPresent()) {
            Notification notification = optionalNotification.get();
            notification.setReadStatus("已读");
            return notificationRepository.save(notification);
        }
        return null;
    }
}
