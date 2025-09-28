package com.altair288.class_management.ObjectStorage.dto;

public class ConnectionDTO {
    private Long id;
    private String name;
    private String provider; // MINIO
    private String endpointUrl;
    private Boolean secureFlag;
    private Boolean pathStyleAccess;
    private Integer defaultPresignExpireSeconds;
    private Boolean active;
    private String lastTestStatus;
    private String lastTestError;

    public ConnectionDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getEndpointUrl() { return endpointUrl; }
    public void setEndpointUrl(String endpointUrl) { this.endpointUrl = endpointUrl; }
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
    public String getLastTestError() { return lastTestError; }
    public void setLastTestError(String lastTestError) { this.lastTestError = lastTestError; }
}
