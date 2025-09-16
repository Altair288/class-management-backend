package com.altair288.class_management.MessageCenter.controller;

import com.altair288.class_management.MessageCenter.enums.NotificationPriority;
import com.altair288.class_management.MessageCenter.enums.NotificationType;
import com.altair288.class_management.MessageCenter.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 简化：直接返回未读列表（可分页后续再加）
    @GetMapping("/inbox")
    public List<Map<String, Object>> inbox(@RequestParam Integer userId, @RequestParam(defaultValue = "50") int limit) {
        return notificationService.listInbox(userId, limit);
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount(@RequestParam Integer userId) {
        return Map.of("unread", notificationService.unreadCount(userId));
    }

    public record MarkReadRequest(Integer userId, List<Long> recipientIds) {}
    @PostMapping("/mark-read")
    public Map<String, Object> markRead(@RequestBody MarkReadRequest req) {
        int changed = notificationService.markReadBatch(req.userId(), req.recipientIds());
        return Map.of("updated", changed);
    }

    public record MarkAllReadRequest(Integer userId) {}
    @PostMapping("/mark-all-read")
    public Map<String, Object> markAllRead(@RequestBody MarkAllReadRequest req) {
        int changed = notificationService.markAllRead(req.userId());
        return Map.of("updated", changed);
    }

    // 管理/测试：手动创建一条通知（实际请假流程中将由业务 Service 调用）
    public record CreateNotificationDTO(
            NotificationType type,
            String title,
            String content,
            NotificationPriority priority,
            String businessRefType,
            String businessRefId,
            String dedupeKey,
            String templateCode,
            String extraJson,
            List<Integer> recipients
    ) {}

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody CreateNotificationDTO dto) {
        Long id = notificationService.createNotification(new NotificationService.CreateRequest(
                dto.type(), dto.title(), dto.content(), dto.priority(), dto.businessRefType(),
                dto.businessRefId(), dto.dedupeKey(), dto.templateCode(), dto.extraJson(), dto.recipients()
        ));
        return Map.of("id", id);
    }
}
