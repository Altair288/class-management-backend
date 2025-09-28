package com.altair288.class_management.ObjectStorage.dto;

public class CreateUploadResponse {
    private Long fileObjectId;
    private String bucketName;
    private String objectKey;
    private String presignUrl; // PUT 上传 URL
    private Integer expireSeconds;

    public CreateUploadResponse() {}

    public Long getFileObjectId() { return fileObjectId; }
    public void setFileObjectId(Long fileObjectId) { this.fileObjectId = fileObjectId; }
    public String getBucketName() { return bucketName; }
    public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getPresignUrl() { return presignUrl; }
    public void setPresignUrl(String presignUrl) { this.presignUrl = presignUrl; }
    public Integer getExpireSeconds() { return expireSeconds; }
    public void setExpireSeconds(Integer expireSeconds) { this.expireSeconds = expireSeconds; }
}
