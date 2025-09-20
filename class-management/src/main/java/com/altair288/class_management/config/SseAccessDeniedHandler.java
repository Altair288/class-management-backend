package com.altair288.class_management.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 针对 SSE 流的 AccessDenied 处理：
 *  - 避免在连接已 commit 后再次写入触发 ServletException 日志噪音
 *  - SSE 端点上只设置状态码，不写 body（保持连接静默关闭/由前端重连逻辑处理）
 */
public class SseAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (isSseRequest(request)) {
            if (!response.isCommitted()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
            // 不写入任何内容，交由前端的 EventSource onerror 触发重连或身份检查
            return;
        }
        if (!response.isCommitted()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
        }
    }

    private boolean isSseRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return (uri != null && uri.contains("/api/notifications/stream")) ||
               (accept != null && accept.contains("text/event-stream"));
    }
}
