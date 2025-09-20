package com.altair288.class_management.MessageCenter.controller;

import com.altair288.class_management.MessageCenter.service.SsePushService;
import com.altair288.class_management.MessageCenter.service.NotificationService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationStreamController {

    private final SsePushService ssePushService;
    private final NotificationService notificationService;

    public NotificationStreamController(SsePushService ssePushService, NotificationService notificationService) {
        this.ssePushService = ssePushService;
        this.notificationService = notificationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam Integer userId) {
        // TODO: 未来这里从认证主体中解析真实 userId，避免被伪造
        SseEmitter emitter = ssePushService.connect(userId);
        try {
            // 延迟少量毫秒，等待前端 init 监听安装完成再发送 snapshot
            new Thread(() -> {
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                try {
                    long unread = notificationService.unreadCount(userId);
                    var inbox = notificationService.listInbox(userId, 20);
                    Map<String,Object> snap = new java.util.LinkedHashMap<>();
                    snap.put("unreadCount", unread);
                    snap.put("notifications", inbox);
                    ssePushService.snapshot(userId, snap);
                } catch (Exception ignored) {}
            }, "sse-snapshot-trigger").start();
        } catch (Exception ignored) {}
        return emitter;
    }
}
