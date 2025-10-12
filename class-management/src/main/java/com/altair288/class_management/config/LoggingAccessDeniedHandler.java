package com.altair288.class_management.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * 可选的调试 AccessDenied 处理器：
 *  - 在 delegate 之前记录一次拒绝信息（不重复写响应）。
 *  - 通过配置属性开关 verboseStack 来控制是否打印堆栈。
 */
public class LoggingAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(LoggingAccessDeniedHandler.class);
    private final AccessDeniedHandler delegate;
    private final boolean verbose;
    private final boolean verboseStack;

    public LoggingAccessDeniedHandler(AccessDeniedHandler delegate, boolean verbose, boolean verboseStack) {
        this.delegate = delegate;
        this.verbose = verbose;
        this.verboseStack = verboseStack;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        if (verbose) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String principal = auth != null ? auth.getName() : "(anonymous)";
            String roles = auth != null && auth.getAuthorities() != null ?
                    auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.joining(",")) : "";
            String path = request.getRequestURI();
            String pattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String method = request.getMethod();
            String query = request.getQueryString();
            String dispatcher = request.getDispatcherType() != null ? request.getDispatcherType().name() : "?";
            boolean asyncStarted = request.isAsyncStarted();
            String remote = request.getRemoteAddr();
            String sessionId = request.getSession(false) != null ? request.getSession(false).getId() : "(no-session)";
            String ua = request.getHeader("User-Agent");
            boolean committed = response.isCommitted();
            boolean ssePath = path != null && path.contains("/api/notifications/stream");
            boolean asyncNoise = ("ASYNC".equals(dispatcher) || asyncStarted) && (committed || ssePath);
            String logLine = String.format("ACCESS_DENIED at %s path='%s' pattern='%s' method=%s query='%s' principal='%s' roles='%s' committed=%s dispatcher=%s asyncStarted=%s session=%s remote=%s ua='%s' msg=%s",
                    Instant.now(), path, pattern, method, query, principal, roles, committed, dispatcher, asyncStarted, sessionId, remote, ua, accessDeniedException.getMessage());
            if (asyncNoise) {
                // 这类多为 SSE 断开后的异步写入或已提交响应上的二次触发，降级为 debug
                if (log.isDebugEnabled()) log.debug(logLine);
            } else {
                log.error(logLine);
                if (verboseStack) {
                    log.error("ACCESS_DENIED stack", accessDeniedException);
                }
            }
        }
        delegate.handle(request, response, accessDeniedException);
    }
}
