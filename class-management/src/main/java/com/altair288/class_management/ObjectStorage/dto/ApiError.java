package com.altair288.class_management.ObjectStorage.dto;

import java.time.Instant;

public class ApiError {
    private Instant timestamp = Instant.now();
    private String path;
    private String code;
    private String message;

    public ApiError() {}
    public ApiError(Instant timestamp, String path, String code, String message) {
        this.timestamp = timestamp;
        this.path = path;
        this.code = code;
        this.message = message;
    }

    public ApiError(String path, String code, String message){
        this.path = path;
        this.code = code;
        this.message = message;
    }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
