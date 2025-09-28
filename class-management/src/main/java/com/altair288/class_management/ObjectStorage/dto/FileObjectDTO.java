package com.altair288.class_management.ObjectStorage.dto;

import java.util.Date;

public class FileObjectDTO {
    private Long id;
    private String bucketName;
    private String objectKey;
    private String originalFilename;
    private String ext;
    private String mimeType;
    private Long sizeBytes;
    private String status;
    private Integer uploaderUserId;
    private String businessRefType;
    private Long businessRefId;
    private Date createdAt;
    private Date completedAt;
    private String downloadUrl; // 可选返回

    public FileObjectDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getExt() { return ext; }
    public void setExt(String ext) { this.ext = ext; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getUploaderUserId() { return uploaderUserId; }
    public void setUploaderUserId(Integer uploaderUserId) { this.uploaderUserId = uploaderUserId; }
    public String getBusinessRefType() { return businessRefType; }
    public void setBusinessRefType(String businessRefType) { this.businessRefType = businessRefType; }
    public Long getBusinessRefId() { return businessRefId; }
    public void setBusinessRefId(Long businessRefId) { this.businessRefId = businessRefId; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
}
