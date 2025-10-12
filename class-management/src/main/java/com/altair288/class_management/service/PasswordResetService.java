package com.altair288.class_management.service;

import com.altair288.class_management.config.PasswordResetProperties;
import com.altair288.class_management.exception.BusinessException;
import com.altair288.class_management.model.PasswordResetToken;
import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.PasswordResetTokenRepository;
import com.altair288.class_management.repository.UserRepository;
import com.altair288.class_management.repository.StudentRepository;
import com.altair288.class_management.repository.TeacherRepository;
import com.altair288.class_management.repository.ParentRepository;
import com.altair288.class_management.util.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;

@Service
public class PasswordResetService {
    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetProperties props;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                PasswordResetProperties props,
                                MailService mailService,
                                StudentRepository studentRepository,
                                TeacherRepository teacherRepository,
                                ParentRepository parentRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
        this.mailService = mailService;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.parentRepository = parentRepository;
    }

    /** 请求重置：即使用户不存在也返回统一消息 */
    @Transactional
    public void requestReset(String identifier, String ip, String ua) {
        Date now = new Date();
        // 简单 IP 限流
        long recent = tokenRepository.countRecentRequestsFromIp(ip, new Date(now.getTime() - 3600_000));
        if (recent >= props.getIpHourlyLimit()) {
            log.warn("[pwd-reset] IP限流触发 ip={} recent={} limit={}", ip, recent, props.getIpHourlyLimit());
            return; // 静默返回
        }
        Optional<User> userOpt = userRepository.findByUsernameOrIdentityNo(identifier);
        if (userOpt.isEmpty()) {
            log.debug("[pwd-reset] 伪装成功发送(用户不存在) identifier={}", identifier);
            return; // 静默
        }
        User user = userOpt.get();
        // 可复用最近未过期 token（减少垃圾数据）
        List<PasswordResetToken> actives = tokenRepository.findActiveTokensForUser(user, now);
        PasswordResetToken token;
        if (!actives.isEmpty()) {
            token = actives.get(0);
            log.info("[pwd-reset] 复用未过期token userId={}", user.getId());
        } else {
            token = new PasswordResetToken();
            token.setUser(user);
            token.setToken(generateTokenValue());
            token.setExpiresAt(new Date(now.getTime() + props.getTokenExpireMinutes() * 60_000L));
            token.setRequestIp(ip);
            token.setUserAgent(truncate(ua, 250));
            tokenRepository.save(token);
        }
        sendResetMail(user, token);
    }

    /** 验证 token，不改变其状态 */
    @Transactional(readOnly = true)
    public PasswordResetToken verifyToken(String tokenValue) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("TOKEN_INVALID", "重置链接无效"));
        if (token.isUsed()) {
            throw new BusinessException("TOKEN_USED", "重置链接已使用");
        }
        if (token.getExpiresAt().before(new Date())) {
            throw new BusinessException("TOKEN_EXPIRED", "重置链接已过期");
        }
        return token;
    }

    /** 使用 token 重置密码 */
    @Transactional
    public void consumeTokenAndReset(String tokenValue, String newPassword) {
        PasswordResetToken token = verifyToken(tokenValue); // 会抛出异常
        User user = token.getUser();
        try {
            PasswordValidator.validatePassword(newPassword);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("PASSWORD_POLICY_VIOLATION", ex.getMessage());
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        token.setUsed(true);
        token.setConsumedAt(new Date());
        tokenRepository.save(token);
        userRepository.save(user);
        log.info("[pwd-reset] 密码重置成功 userId={}", user.getId());
    }

    private void sendResetMail(User user, PasswordResetToken token) {
        String link = props.getFrontendBaseUrl().replaceAll("/+$", "") + "/reset-password?token=" + token.getToken();
        String subject = "密码重置请求";
        String body = "<p>您收到此邮件是因为提交了密码重置请求（或有人尝试）。如果不是您本人操作，可忽略本邮件。</p>" +
                "<p>点击以下链接设置新密码（" + props.getTokenExpireMinutes() + "分钟内有效）:</p>" +
                "<p><a href='" + link + "'>" + link + "</a></p>" +
                "<p>如果无法点击，请复制链接到浏览器。</p>";
        try {
            mailService.sendHtmlMail(props.getMailFrom(), resolveUserEmail(user), subject, body);
            log.info("[pwd-reset] 邮件已发送 userId={}", user.getId());
        } catch (Exception e) {
            log.error("[pwd-reset] 邮件发送失败 userId={} err={}", user.getId(), e.getMessage());
        }
    }

    // 根据 userType -> relatedId 到对应实体取 email；若无则降级 username
    private String resolveUserEmail(User user) {
        if (user.getUserType() == null || user.getRelatedId() == null) return user.getUsername();
        return switch (user.getUserType()) {
            case STUDENT -> studentRepository.findById(user.getRelatedId())
                    .map(s -> s.getEmail() != null && !s.getEmail().isBlank() ? s.getEmail() : user.getUsername())
                    .orElse(user.getUsername());
            case TEACHER -> teacherRepository.findById(user.getRelatedId())
                    .map(t -> t.getEmail() != null && !t.getEmail().isBlank() ? t.getEmail() : user.getUsername())
                    .orElse(user.getUsername());
            case PARENT -> parentRepository.findById(user.getRelatedId())
                    .map(p -> p.getEmail() != null && !p.getEmail().isBlank() ? p.getEmail() : user.getUsername())
                    .orElse(user.getUsername());
            default -> user.getUsername();
        };
    }

    private String truncate(String v, int max) { return v == null ? null : (v.length() <= max ? v : v.substring(0, max)); }

    private String generateTokenValue() {
        byte[] buf = new byte[48]; // 64Base64≈64字符
        random.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
