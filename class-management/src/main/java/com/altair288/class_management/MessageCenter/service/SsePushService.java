package com.altair288.class_management.MessageCenter.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SsePushService {

    private static final Logger log = LoggerFactory.getLogger(SsePushService.class);

    // userId -> emitters
    private final Map<Integer, List<SseEmitter>> connections = new ConcurrentHashMap<>();

    // 30 分钟超时；前端可在即将超时时自动重连
    private static final long TIMEOUT = 30L * 60 * 1000;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "sse-heartbeat");
        t.setDaemon(true);
        return t;
    });
    private final Map<SseEmitter, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();
    private static final long HEARTBEAT_INTERVAL_SECONDS = 30; // 常规心跳间隔
    private static final long HEARTBEAT_INITIAL_DELAY_SECONDS = 5; // 首次心跳提前，帮助前端快速确认活性

    public SsePushService() {}

    public SseEmitter connect(Integer userId) {
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        List<SseEmitter> list = connections.get(userId);
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
            connections.put(userId, list);
        } else if (!list.isEmpty()) {
            // 强制只保持一个活动连接：关闭现有全部
            for (SseEmitter old : list) {
                try { complete(userId, old); } catch (Exception ignored) {}
            }
            list.clear();
        }
        list.add(emitter);
        emitter.onTimeout(() -> {
            if (log.isDebugEnabled()) log.debug("[SSE] timeout userId={} emitter={}", userId, System.identityHashCode(emitter));
            complete(userId, emitter);
        });
        emitter.onCompletion(() -> {
            if (log.isDebugEnabled()) log.debug("[SSE] completion userId={} emitter={}", userId, System.identityHashCode(emitter));
            complete(userId, emitter);
        });
        if (log.isDebugEnabled()) {
            log.debug("[SSE] connect userId={} activeNow={} emitterHash={} (dedup applied)", userId, list.size(), System.identityHashCode(emitter));
        }
        try {
            emitter.send(SseEmitter.event().name("init").data("ok"));
        } catch (Exception ignored) {}
        // 安排心跳：定期发送 ping，刷新连接与会话
        ScheduledFuture<?> hb = scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event().name("ping").data(System.currentTimeMillis()));
            } catch (Exception e) {
                if (log.isDebugEnabled()) log.debug("[SSE] heartbeat send failed userId={} emitterHash={} err={}", userId, System.identityHashCode(emitter), e.getClass().getSimpleName());
                complete(userId, emitter);
            }
        }, HEARTBEAT_INITIAL_DELAY_SECONDS, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);
        heartbeatTasks.put(emitter, hb);
        return emitter;
    }

    public void push(Integer userId, Map<String, Object> payload) {
        List<SseEmitter> list = connections.get(userId);
        if (list == null || list.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("[SSE] push skipped, no active emitter for userId={}", userId);
            }
            return;
        }
        for (SseEmitter e : list) {
            try {
                e.send(SseEmitter.event().name("notification").data(payload));
            } catch (Exception ex) {
                if (isAccessDenied(ex)) {
                    log.debug("SSE push denied for user {}: {}", userId, ex.getClass().getSimpleName());
                } else {
                    log.debug("SSE push failed for user {}: {}", userId, ex.toString());
                }
                complete(userId, e);
            }
        }
    }

    /**
     * 发送一次 snapshot（未读数 + 最近列表）用于前端在 init 后快速同步状态。
     */
    public void snapshot(Integer userId, Map<String,Object> snapshot) {
        List<SseEmitter> list = connections.get(userId);
        if (list == null || list.isEmpty()) return;
        for (SseEmitter e : list) {
            try {
                e.send(SseEmitter.event().name("snapshot").data(snapshot));
            } catch (Exception ex) {
                complete(userId, e);
            }
        }
    }

    public void broadcast(Collection<Integer> userIds, Map<String,Object> payload) {
        if (userIds == null) return;
        for (Integer uid : userIds) push(uid, payload);
    }

    private void complete(Integer userId, SseEmitter emitter) {
        List<SseEmitter> list = connections.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) connections.remove(userId);
        }
        ScheduledFuture<?> hb = heartbeatTasks.remove(emitter);
        if (hb != null) hb.cancel(false);
        if (log.isDebugEnabled()) {
            log.debug("[SSE] cleanup userId={} emitterHash={} remaining={} ", userId, System.identityHashCode(emitter), list==null?0:list.size());
        }
    }
    private boolean isAccessDenied(Throwable ex) {
        return ex instanceof AccessDeniedException || ex instanceof AuthorizationDeniedException;
    }
}

