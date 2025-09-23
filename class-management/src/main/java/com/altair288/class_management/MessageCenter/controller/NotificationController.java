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

    // 历史记录（包含已读）分页查询: /api/notifications/history?userId=1&page=0&size=50&readStatus=all|true|false
    @GetMapping("/history")
    public Map<String,Object> history(@RequestParam Integer userId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size,
                                      @RequestParam(required = false) String readStatus) {
        Boolean rs = null;
        if ("true".equalsIgnoreCase(readStatus)) rs = true; else if ("false".equalsIgnoreCase(readStatus)) rs = false;
        return notificationService.listHistory(userId, page, size, rs);
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

    // 模板创建接口：前端只传 templateCode + variables + recipients
    public record CreateFromTemplateDTO(
            NotificationType type,
            String templateCode,
            Map<String,Object> variables,
            NotificationPriority priority,
            String businessRefType,
            String businessRefId,
            String dedupeKey,
            List<Integer> recipients
    ) {}

    @PostMapping("/create-template")
    public Map<String,Object> createFromTemplate(@RequestBody CreateFromTemplateDTO dto) {
        Long id = notificationService.createFromTemplate(new NotificationService.TemplateRequest(
                dto.type(), dto.templateCode(), dto.variables(), dto.priority(),
                dto.businessRefType(), dto.businessRefId(), dto.dedupeKey(), dto.recipients()
        ));
        return Map.of("id", id);
    }
}
