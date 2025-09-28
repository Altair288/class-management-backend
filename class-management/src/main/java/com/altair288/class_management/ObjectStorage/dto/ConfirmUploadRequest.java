package com.altair288.class_management.ObjectStorage.dto;

import jakarta.validation.constraints.*;

public class ConfirmUploadRequest {
    @NotNull
    private Long fileObjectId;
    private Long sizeBytes;
    private String mimeType;

    public ConfirmUploadRequest() {}

    public Long getFileObjectId() { return fileObjectId; }
    public void setFileObjectId(Long fileObjectId) { this.fileObjectId = fileObjectId; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
}
