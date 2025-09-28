package com.altair288.class_management.ObjectStorage.dto;

import jakarta.validation.constraints.*;

public class CreateUploadRequest {
    @NotBlank
    private String bucketPurpose; // 对应 file_storage_config.bucket_purpose
    @NotBlank
    private String originalFilename;
    @NotBlank
    private String businessRefType; // LEAVE_REQUEST
    @NotNull
    private Long businessRefId;
    private Long expectedSize; // 可选 前端告知大小

    public CreateUploadRequest() {}

    public String getBucketPurpose() { return bucketPurpose; }
    public void setBucketPurpose(String bucketPurpose) { this.bucketPurpose = bucketPurpose; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getBusinessRefType() { return businessRefType; }
    public void setBusinessRefType(String businessRefType) { this.businessRefType = businessRefType; }
    public Long getBusinessRefId() { return businessRefId; }
    public void setBusinessRefId(Long businessRefId) { this.businessRefId = businessRefId; }
    public Long getExpectedSize() { return expectedSize; }
    public void setExpectedSize(Long expectedSize) { this.expectedSize = expectedSize; }
}
