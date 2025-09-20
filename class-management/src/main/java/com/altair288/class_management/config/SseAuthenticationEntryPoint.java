package com.altair288.class_management.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 针对 SSE 请求的认证入口：如果是 /api/notifications/stream 返回 401 空 body，
 * 避免返回 HTML/JSON 造成前端解析异常或再次触发转换错误。
 */
public class SseAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String uri = request.getRequestURI();
        if (uri != null && uri.contains("/api/notifications/stream")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.flushBuffer();
            return;
        }
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
