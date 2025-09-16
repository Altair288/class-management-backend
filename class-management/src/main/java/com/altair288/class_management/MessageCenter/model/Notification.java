package com.altair288.class_management.MessageCenter.model;

import com.altair288.class_management.MessageCenter.enums.NotificationPriority;
import com.altair288.class_management.MessageCenter.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_type_created", columnList = "type, created_at"),
        @Index(name = "idx_business_ref", columnList = "business_ref_type, business_ref_id")
})
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false, length = 200)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "channels_bitmask", nullable = false)
    private Integer channelsBitmask = 1; // 默认 INBOX

    @Column(name = "dedupe_key", length = 150, unique = true)
    private String dedupeKey;

    @Column(name = "business_ref_type", length = 50)
    private String businessRefType;

    @Column(name = "business_ref_id", length = 64)
    private String businessRefId;

    @Column(name = "template_code", length = 64)
    private String templateCode;

    @Column(name = "template_version")
    private Integer templateVersion;

    @Lob
    @Column(name = "rendered_variables_json")
    private String renderedVariablesJson; // 渲染时使用变量的快照
    public Integer getTemplateVersion() { return templateVersion; }
    public void setTemplateVersion(Integer templateVersion) { this.templateVersion = templateVersion; }
    public String getRenderedVariablesJson() { return renderedVariablesJson; }
    public void setRenderedVariablesJson(String renderedVariablesJson) { this.renderedVariablesJson = renderedVariablesJson; }

    @Lob
    @Column(name = "extra_json")
    private String extraJson; // 采用 String 保存 JSON，避免方言 JSON 兼容问题

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // Getters and Setters (手写避免依赖 Lombok 生成)
    public Long getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public Integer getChannelsBitmask() {
        return channelsBitmask;
    }

    public void setChannelsBitmask(Integer channelsBitmask) {
        this.channelsBitmask = channelsBitmask;
    }

    public String getDedupeKey() {
        return dedupeKey;
    }

    public void setDedupeKey(String dedupeKey) {
        this.dedupeKey = dedupeKey;
    }

    public String getBusinessRefType() {
        return businessRefType;
    }

    public void setBusinessRefType(String businessRefType) {
        this.businessRefType = businessRefType;
    }

    public String getBusinessRefId() {
        return businessRefId;
    }

    public void setBusinessRefId(String businessRefId) {
        this.businessRefId = businessRefId;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getExtraJson() {
        return extraJson;
    }

    public void setExtraJson(String extraJson) {
        this.extraJson = extraJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
