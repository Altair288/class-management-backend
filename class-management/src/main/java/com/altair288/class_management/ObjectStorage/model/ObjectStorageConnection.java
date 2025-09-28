package com.altair288.class_management.ObjectStorage.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "object_storage_connection")
public class ObjectStorageConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 唯一别名
    private String provider; // MINIO / S3
    @Column(name = "endpoint_url")
    private String endpointUrl;

    @Column(name = "access_key_encrypted")
    private String accessKeyEncrypted;
    @Column(name = "secret_key_encrypted")
    private String secretKeyEncrypted;

    @Column(name = "secure_flag")
    private Boolean secureFlag = true;
    @Column(name = "path_style_access")
    private Boolean pathStyleAccess = true;
    @Column(name = "default_presign_expire_seconds")
    private Integer defaultPresignExpireSeconds = 600;
    private Boolean active = true;

    @Column(name = "last_test_status")
    private String lastTestStatus = "UNKNOWN"; // SUCCESS / FAIL / UNKNOWN
    @Column(name = "last_test_time")
    private Date lastTestTime;
    @Column(name = "last_test_error")
    private String lastTestError;

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
    public void preUpdate(){
        updatedAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
    public String getAccessKeyEncrypted() { return accessKeyEncrypted; }
    public void setAccessKeyEncrypted(String accessKeyEncrypted) { this.accessKeyEncrypted = accessKeyEncrypted; }
    public String getSecretKeyEncrypted() { return secretKeyEncrypted; }
    public void setSecretKeyEncrypted(String secretKeyEncrypted) { this.secretKeyEncrypted = secretKeyEncrypted; }
    public Boolean getSecureFlag() { return secureFlag; }
    public void setSecureFlag(Boolean secureFlag) { this.secureFlag = secureFlag; }
    public Boolean getPathStyleAccess() { return pathStyleAccess; }
    public void setPathStyleAccess(Boolean pathStyleAccess) { this.pathStyleAccess = pathStyleAccess; }
    public Integer getDefaultPresignExpireSeconds() { return defaultPresignExpireSeconds; }
    public void setDefaultPresignExpireSeconds(Integer defaultPresignExpireSeconds) { this.defaultPresignExpireSeconds = defaultPresignExpireSeconds; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getLastTestStatus() { return lastTestStatus; }
    public void setLastTestStatus(String lastTestStatus) { this.lastTestStatus = lastTestStatus; }
    public Date getLastTestTime() { return lastTestTime; }
    public void setLastTestTime(Date lastTestTime) { this.lastTestTime = lastTestTime; }
    public String getLastTestError() { return lastTestError; }
    public void setLastTestError(String lastTestError) { this.lastTestError = lastTestError; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
