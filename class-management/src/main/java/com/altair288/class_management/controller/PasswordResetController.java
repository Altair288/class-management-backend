package com.altair288.class_management.controller;

import com.altair288.class_management.exception.BusinessException;
import com.altair288.class_management.model.PasswordResetToken;
import com.altair288.class_management.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class PasswordResetController {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, String>> forgot(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String identifier = body.getOrDefault("identifier", "").trim();
        if (identifier.isEmpty()) throw new BusinessException("IDENTIFIER_EMPTY", "标识不能为空");
        passwordResetService.requestReset(identifier, clientIp(req), req.getHeader("User-Agent"));
        return ResponseEntity.ok(Map.of("message", "如果账户存在，将发送一封包含重置链接的邮件"));
    }

    @GetMapping("/reset/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestParam("token") String token) {
        PasswordResetToken prt = passwordResetService.verifyToken(token);
        return ResponseEntity.ok(Map.of("valid", true, "expiresAt", prt.getExpiresAt()));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> reset(@RequestBody Map<String, String> body) {
        String token = body.getOrDefault("token", "");
        String newPassword = body.getOrDefault("newPassword", "");
        if (token.isEmpty() || newPassword.isEmpty()) throw new BusinessException("PARAM_MISSING", "参数缺失");
        passwordResetService.consumeTokenAndReset(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "密码已重置"));
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
