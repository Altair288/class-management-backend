package com.altair288.class_management.ObjectStorage.dto;

import jakarta.validation.constraints.*;
import java.util.List;

/**
 * 创建或更新 file_storage_config 的请求体。
 * allowedExtensions / allowedMimeTypes 若为空则表示不限制，后端持久化为 []
 */
public class SaveFileStorageConfigRequest {
    private Integer id; // 可选，存在则更新

    @NotBlank
    private String bucketName;
    @NotBlank
    private String bucketPurpose;
    @NotNull
    private Long connectionId;

    private String basePath;
    @NotNull
    private Long maxFileSize; // Byte

    private List<String> allowedExtensions; // 可空
    private List<String> allowedMimeTypes;  // 可空
    @NotNull
    private Integer retentionDays;
    @NotNull
    private Boolean autoCleanup;
    @NotNull
    private Boolean enabled;

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
}
