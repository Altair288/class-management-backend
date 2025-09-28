package com.altair288.class_management.ObjectStorage.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "file_object", indexes = {
        @Index(name = "idx_storage_config", columnList = "storage_config_id"),
        @Index(name = "idx_bucket_key", columnList = "bucket_name,object_key"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_biz_ref", columnList = "business_ref_type,business_ref_id"),
        @Index(name = "idx_uploader", columnList = "uploader_user_id"),
        @Index(name = "uk_storage_object", columnList = "storage_config_id,object_key", unique = true)
})
public class FileObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storage_config_id")
    private Integer storageConfigId;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "object_key")
    private String objectKey;

    @Column(name = "original_filename")
    private String originalFilename;

    private String ext;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    private String status; // UPLOADING/COMPLETED/FAILED/DELETED

    @Column(name = "uploader_user_id")
    private Integer uploaderUserId;

    @Column(name = "business_ref_type")
    private String businessRefType;

    @Column(name = "business_ref_id")
    private Long businessRefId;

    @Column(name = "created_at")
    private Date createdAt;
    @Column(name = "completed_at")
    private Date completedAt;
    @Column(name = "deleted_at")
    private Date deletedAt;

    @PrePersist
    public void prePersist(){
        if(createdAt==null) createdAt=new Date();
        if(status==null) status="UPLOADING";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStorageConfigId() { return storageConfigId; }
    public void setStorageConfigId(Integer storageConfigId) { this.storageConfigId = storageConfigId; }
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
    public Date getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Date deletedAt) { this.deletedAt = deletedAt; }
}
