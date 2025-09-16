package com.altair288.class_management.MessageCenter.service;

import com.altair288.class_management.MessageCenter.enums.NotificationChannel;
import com.altair288.class_management.MessageCenter.enums.NotificationPriority;
import com.altair288.class_management.MessageCenter.enums.NotificationType;
import com.altair288.class_management.MessageCenter.model.Notification;
import com.altair288.class_management.MessageCenter.model.NotificationPreference;
import com.altair288.class_management.MessageCenter.model.NotificationRecipient;
import com.altair288.class_management.MessageCenter.repository.NotificationPreferenceRepository;
import com.altair288.class_management.MessageCenter.repository.NotificationRecipientRepository;
import com.altair288.class_management.MessageCenter.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository recipientRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationRecipientRepository recipientRepository,
                               NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.recipientRepository = recipientRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public record CreateRequest(
            NotificationType type,
            String title,
            String content,
            NotificationPriority priority,
            String businessRefType,
            String businessRefId,
            String dedupeKey,
            String templateCode,
            String extraJson,
            Collection<Integer> recipients
    ) {}

    @Transactional
    public Long createNotification(CreateRequest req) {
        if (req.type() == null) throw new IllegalArgumentException("type 不能为空");
        // 幂等：dedupeKey 存在直接返回
        if (req.dedupeKey() != null && !req.dedupeKey().isBlank()) {
            var exist = notificationRepository.findByDedupeKey(req.dedupeKey());
            if (exist.isPresent()) return exist.get().getId();
        }
    Notification n = new Notification();
    n.setType(req.type());
    n.setTitle(Objects.requireNonNullElse(req.title(), req.type().name()));
    n.setContent(Objects.requireNonNullElse(req.content(), ""));
    n.setPriority(req.priority() == null? NotificationPriority.NORMAL : req.priority());
    n.setChannelsBitmask(1);
    n.setDedupeKey(req.dedupeKey());
    n.setBusinessRefType(req.businessRefType());
    n.setBusinessRefId(req.businessRefId());
    n.setTemplateCode(req.templateCode());
    n.setExtraJson(req.extraJson());
        n = notificationRepository.save(n);

        if (req.recipients() != null && !req.recipients().isEmpty()) {
            // 批量读取偏好
            List<NotificationPreference> prefs = preferenceRepository.findByUserId(req.recipients().iterator().next());
            Map<String, List<NotificationPreference>> grouped = prefs.stream()
                    .collect(Collectors.groupingBy(p -> p.getNotificationType() + "#" + p.getChannel()));
            List<NotificationRecipient> toSave = new ArrayList<>();
            for (Integer uid : new LinkedHashSet<>(req.recipients())) {
                boolean inbox = resolveChannelEnabled(uid, req.type(), NotificationChannel.INBOX, grouped);
                boolean email = resolveChannelEnabled(uid, req.type(), NotificationChannel.EMAIL, grouped);
        NotificationRecipient r = new NotificationRecipient();
        r.setNotification(n);
        r.setUserId(uid);
        r.setInboxEnabled(inbox);
        r.setEmailEnabled(email);
        r.setEmailSent(false);
        r.setReadStatus(false);
                toSave.add(r);
            }
            recipientRepository.saveAll(toSave);
        }
        return n.getId();
    }

    private boolean resolveChannelEnabled(Integer userId, NotificationType type, NotificationChannel ch,
                                          Map<String, List<NotificationPreference>> prefGrouped) {
        // 当前实现：INBOX 默认 true, EMAIL 默认 false。用户偏好可覆盖。
        String key = type.name() + "#" + ch.name();
        List<NotificationPreference> list = prefGrouped.get(key);
        if (list != null) {
            return list.stream().filter(p -> Objects.equals(p.getUserId(), userId))
                    .map(NotificationPreference::getEnabled).findFirst().orElse(defaultChannel(ch));
        }
        return defaultChannel(ch);
    }

    private boolean defaultChannel(NotificationChannel ch) {
        return ch == NotificationChannel.INBOX; // 只有站内默认开启
    }

    @Transactional
    public int markReadBatch(Integer userId, Collection<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) return 0;
        List<NotificationRecipient> list = recipientRepository.findAllById(recipientIds);
        int changed = 0;
        Instant now = Instant.now();
        for (NotificationRecipient r : list) {
            if (!Objects.equals(r.getUserId(), userId)) continue; // 只能标记自己的
            if (!Boolean.TRUE.equals(r.getReadStatus())) {
                r.setReadStatus(true);
                r.setReadAt(now);
                changed++;
            }
        }
        if (changed > 0) recipientRepository.saveAll(list);
        return changed;
    }

    @Transactional
    public int markAllRead(Integer userId) {
        List<NotificationRecipient> unread = recipientRepository.findByUserIdAndReadStatusFalseOrderByIdDesc(userId);
        Instant now = Instant.now();
        for (NotificationRecipient r : unread) {
            r.setReadStatus(true);
            r.setReadAt(now);
        }
        recipientRepository.saveAll(unread);
        return unread.size();
    }

    @Transactional
    public List<Map<String, Object>> listInbox(Integer userId, int limit) {
        List<NotificationRecipient> recs = recipientRepository.findByUserIdAndReadStatusFalseOrderByIdDesc(userId);
        if (limit > 0 && recs.size() > limit) recs = recs.subList(0, limit);
        List<Map<String, Object>> result = new ArrayList<>();
        for (NotificationRecipient r : recs) {
            Notification n = r.getNotification();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("recipientId", r.getId());
            row.put("notificationId", n.getId());
            row.put("type", n.getType().name());
            row.put("title", n.getTitle());
            row.put("content", n.getContent());
            row.put("priority", n.getPriority().name());
            row.put("businessRefType", n.getBusinessRefType());
            row.put("businessRefId", n.getBusinessRefId());
            row.put("createdAt", n.getCreatedAt());
            result.add(row);
        }
        return result;
    }

    public long unreadCount(Integer userId) {
        return recipientRepository.countByUserIdAndReadStatusFalse(userId);
    }
}
