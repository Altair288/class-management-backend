package com.altair288.class_management.ObjectStorage.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "file_storage_config")
public class FileStorageConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bucket_name")
    private String bucketName;
    @Column(name = "bucket_purpose")
    private String bucketPurpose;

    @Column(name = "connection_id")
    private Long connectionId;

    @Column(name = "base_path")
    private String basePath;

    @Column(name = "max_file_size")
    private Long maxFileSize;

    @Column(name = "allowed_extensions", columnDefinition = "json")
    private String allowedExtensions;
    @Column(name = "allowed_mime_types", columnDefinition = "json")
    private String allowedMimeTypes;

    @Column(name = "retention_days")
    private Integer retentionDays;

    @Column(name = "auto_cleanup")
    private Boolean autoCleanup;

    private Boolean enabled;

    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    public void prePersist(){
        Date now = new Date();
        if(createdAt==null) createdAt = now;
        if(updatedAt==null) updatedAt = now;
    }
    @PreUpdate
    public void preUpdate(){ updatedAt = new Date(); }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getBucketPurpose() { return bucketPurpose; }
    public void setBucketPurpose(String bucketPurpose) { this.bucketPurpose = bucketPurpose; }
    public Long getConnectionId() { return connectionId; }
    public void setConnectionId(Long connectionId) { this.connectionId = connectionId; }
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }
    public Long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(Long maxFileSize) { this.maxFileSize = maxFileSize; }
    public String getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(String allowedExtensions) { this.allowedExtensions = allowedExtensions; }
    public String getAllowedMimeTypes() { return allowedMimeTypes; }
    public void setAllowedMimeTypes(String allowedMimeTypes) { this.allowedMimeTypes = allowedMimeTypes; }
    public Integer getRetentionDays() { return retentionDays; }
    public void setRetentionDays(Integer retentionDays) { this.retentionDays = retentionDays; }
    public Boolean getAutoCleanup() { return autoCleanup; }
    public void setAutoCleanup(Boolean autoCleanup) { this.autoCleanup = autoCleanup; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
