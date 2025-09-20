package com.altair288.class_management.MessageCenter.controller;

import com.altair288.class_management.MessageCenter.service.SsePushService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications/dev")
public class DevNotificationController {

    private final SsePushService ssePushService;

    public DevNotificationController(SsePushService ssePushService) {
        this.ssePushService = ssePushService;
    }

    @GetMapping("/push")
    public Map<String,Object> push(@RequestParam Integer userId, @RequestParam(defaultValue = "DevTest") String title) {
        Map<String,Object> payload = new LinkedHashMap<>();
        long now = System.currentTimeMillis();
        payload.put("notificationId", now); // 临时用时间戳代表id
        payload.put("recipientId", -1);
        payload.put("title", title);
        payload.put("content", "Dev push at " + now);
        payload.put("type", "SYSTEM");
        payload.put("priority", "NORMAL");
    payload.put("createdAt", java.time.Instant.now());
    // 不放 unreadCount 让前端走乐观+1 （可在真实推送里附加）
        ssePushService.push(userId, payload);
        return payload;
    }
}
