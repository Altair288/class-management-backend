package com.altair288.class_management.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.password-reset")
public class PasswordResetProperties {
    /** token 过期分钟数 */
    private int tokenExpireMinutes = 30;
    /** 每IP每小时请求上限（简单限流，后续可切换 Redis） */
    private int ipHourlyLimit = 20;
    /** 前端基址，用于拼接重置链接 */
    private String frontendBaseUrl = "http://localhost:3000";
    /** 邮件发件人 */
    private String mailFrom = "noreply@example.com";

    public int getTokenExpireMinutes() { return tokenExpireMinutes; }
    public void setTokenExpireMinutes(int tokenExpireMinutes) { this.tokenExpireMinutes = tokenExpireMinutes; }
    public int getIpHourlyLimit() { return ipHourlyLimit; }
    public void setIpHourlyLimit(int ipHourlyLimit) { this.ipHourlyLimit = ipHourlyLimit; }
    public String getFrontendBaseUrl() { return frontendBaseUrl; }
    public void setFrontendBaseUrl(String frontendBaseUrl) { this.frontendBaseUrl = frontendBaseUrl; }
    public String getMailFrom() { return mailFrom; }
    public void setMailFrom(String mailFrom) { this.mailFrom = mailFrom; }
}
