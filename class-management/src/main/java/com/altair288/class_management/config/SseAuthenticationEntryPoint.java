package com.altair288.class_management.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

/**
 * 针对 SSE 请求的认证入口：如果是 /api/notifications/stream 返回 401 空 body，
 * 避免返回 HTML/JSON 造成前端解析异常或再次触发转换错误。
 */
public class SseAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger logger = Logger.getLogger(SseAuthenticationEntryPoint.class.getName());

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        String uri = request.getRequestURI();
        boolean sse = uri != null && uri.contains("/api/notifications/stream");
        if (sse) {
            // 会话过期或未认证时，避免 flushBuffer，直接设置 401，前端 EventSource 会触发 onerror -> 可做重登或刷新。
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                // 提供一个可供前端判断的轻量 JSON，而不是空 body，方便区分网络错误 vs 认证过期
                String json = "{\"code\":401,\"reason\":\"UNAUTHORIZED\",\"message\":\"Session expired or not logged in\"}";
                response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            } else {
                logger.fine("SSE unauthorized but response already committed.");
            }
            return;
        }
        // 非 SSE：沿用标准 sendError
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
