package com.altair288.class_management.ObjectStorage.dto;

import java.util.Date;
import java.util.List;

/**
 * 对外返回的存储配置 DTO
 */
public class FileStorageConfigDTO {
    private Integer id;
    private String bucketName;
    private String bucketPurpose;
    private Long connectionId;
    private String basePath;
    private Long maxFileSize;
    private List<String> allowedExtensions; // 去重小写
    private List<String> allowedMimeTypes;  // 去重小写
    private Integer retentionDays;
    private Boolean autoCleanup;
    private Boolean enabled;
    private Date createdAt;
    private Date updatedAt;

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
    public List<String> getAllowedExtensions() { return allowedExtensions; }
    public void setAllowedExtensions(List<String> allowedExtensions) { this.allowedExtensions = allowedExtensions; }
    public List<String> getAllowedMimeTypes() { return allowedMimeTypes; }
    public void setAllowedMimeTypes(List<String> allowedMimeTypes) { this.allowedMimeTypes = allowedMimeTypes; }
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
