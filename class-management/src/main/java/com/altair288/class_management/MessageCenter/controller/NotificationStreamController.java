package com.altair288.class_management.MessageCenter.controller;

import com.altair288.class_management.MessageCenter.service.SsePushService;
import com.altair288.class_management.MessageCenter.service.NotificationService;
import com.altair288.class_management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationStreamController {

    private static final Logger log = LoggerFactory.getLogger(NotificationStreamController.class);
    private final SsePushService ssePushService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final ExecutorService snapshotPool = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "sse-snapshot-pool");
        t.setDaemon(true);
        return t;
    });

    public NotificationStreamController(SsePushService ssePushService, NotificationService notificationService, UserRepository userRepository) {
        this.ssePushService = ssePushService;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(required = false) Integer userIdIgnored) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated SSE access");
        }
        String principalName = auth.getName();
        Integer tmpId = null;
        try {
            var opt = userRepository.findByUsernameOrIdentityNo(principalName);
            if (opt.isPresent()) {
                tmpId = opt.get().getId();
            } else {
                try { tmpId = Integer.valueOf(principalName); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            log.warn("[SSE] principal resolve error principal={} err={}", principalName, e.toString());
        }
        if (tmpId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot resolve userId for principal");
        }
        final Integer resolvedId = tmpId;
        SseEmitter emitter = ssePushService.connect(resolvedId);
        // 设置一些典型防缓冲头（某些容器会忽略，但尽量声明）
        try {
            emitter.send(SseEmitter.event().name("prelude").data("ready")); // 触发早期 flush（可选）
        } catch (Exception ignored) {}
        if (log.isDebugEnabled()) {
            log.debug("[SSE] stream established principal={} internalId={}", principalName, resolvedId);
        }

        // 3. 使用后台调度而不是手动 new Thread，避免过多瞬时线程（这里仍简单使用线程，后续可迁移到任务执行器）
        snapshotPool.submit(() -> {
            try { Thread.sleep(40); } catch (InterruptedException ignored) {}
            try {
                long unread = notificationService.unreadCount(resolvedId);
                var inbox = notificationService.listInbox(resolvedId, 20);
                Map<String,Object> snap = new java.util.LinkedHashMap<>();
                snap.put("unreadCount", unread);
                snap.put("notifications", inbox);
                ssePushService.snapshot(resolvedId, snap);
            } catch (Exception ex) {
                if (log.isDebugEnabled()) log.debug("[SSE] snapshot failed userId={} err={}", resolvedId, ex.getClass().getSimpleName());
            }
        });
        return emitter;
    }
}
