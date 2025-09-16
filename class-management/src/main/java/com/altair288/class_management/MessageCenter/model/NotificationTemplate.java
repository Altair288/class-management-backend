package com.altair288.class_management.MessageCenter.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification_template", indexes = {
    @Index(name = "idx_code_status", columnList = "code,status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_code_channel_version", columnNames = {"code","channel","version"})
})
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(length = 20)
    private String channel; // NULL=通用

    @Column(nullable = false)
    private Integer version = 1;

    @Column(nullable = false, length = 16)
    private String status = "ACTIVE"; // DRAFT / ACTIVE / INACTIVE

    @Lob
    @Column(name = "title_template", nullable = false)
    private String titleTemplate;

    @Lob
    @Column(name = "content_template", nullable = false)
    private String contentTemplate;

    @Lob
    @Column(name = "sample_variables")
    private String sampleVariables;

    @Column(length = 255)
    private String remark;

    @Column(name = "effective_at")
    private java.time.Instant effectiveAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist(){
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate(){
        this.updatedAt = Instant.now();
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTitleTemplate() { return titleTemplate; }
    public void setTitleTemplate(String titleTemplate) { this.titleTemplate = titleTemplate; }
    public String getContentTemplate() { return contentTemplate; }
    public void setContentTemplate(String contentTemplate) { this.contentTemplate = contentTemplate; }
    public String getSampleVariables() { return sampleVariables; }
    public void setSampleVariables(String sampleVariables) { this.sampleVariables = sampleVariables; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public java.time.Instant getEffectiveAt() { return effectiveAt; }
    public void setEffectiveAt(java.time.Instant effectiveAt) { this.effectiveAt = effectiveAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
