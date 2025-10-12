package com.altair288.class_management.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    /** 可为空（例如非 prod 环境未配置 mail 时不创建 JavaMailSender） */
    private final JavaMailSender mailSender;

    @Value("${mail.provider:smtp}")
    private String provider; // smtp | worker | resend
    @Value("${mail.worker.base-url:}")
    private String workerBaseUrl;
    @Value("${mail.worker.hmac-secret:}")
    private String workerHmacSecret;

    @Value("${mail.resend.api-key:}")
    private String resendApiKey;
    // 可选：若想统一 from，可配置 mail.resend.from / display-name；当前调用方仍传入 from 参数

    private final WebClient webClient = WebClient.builder().build();

    public MailService(@Autowired(required = false) @Nullable JavaMailSender mailSender) {
        this.mailSender = mailSender; // 允许为 null，发送时降级
    }

    public void sendHtmlMail(String from, String to, String subject, String html) throws MessagingException {
        if ("worker".equalsIgnoreCase(provider)) {
            sendViaWorker(from, to, subject, html);
            return;
        }
        if ("resend".equalsIgnoreCase(provider)) {
            sendViaResend(from, to, subject, html);
            return;
        }
        // 默认 SMTP
        if (mailSender == null) {
            log.warn("[mail] JavaMailSender 不存在，跳过邮件发送 subject={} to={} (provider={})", subject, to, provider);
            return; // no-op
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        mailSender.send(message);
    }

    private void sendViaWorker(String from, String to, String subject, String html) {
        if (workerBaseUrl == null || workerBaseUrl.isBlank()) {
            log.error("[mail-worker] base-url 未配置，放弃发送 to={}", to);
            return;
        }
        if (workerHmacSecret == null || workerHmacSecret.isBlank()) {
            log.error("[mail-worker] hmac-secret 未配置，放弃发送 to={}", to);
            return;
        }
        long now = System.currentTimeMillis();
        String path = "/mail/password-reset"; // 固定 endpoint，需与 Worker 对齐
        Map<String, Object> payload = Map.of(
                "to", to,
                "subject", subject,
                "html", html
        );
        String body = JsonUtil.toJson(payload);
        String signing = now + "." + body;
        String sig;
        try { sig = hmacSha256Base64(workerHmacSecret, signing); } catch (Exception e) { log.error("[mail-worker] HMAC失败", e); return; }

        try {
            Mono<String> resp = webClient.post()
                    .uri(workerBaseUrl.replaceAll("/+$", "") + path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Timestamp", String.valueOf(now))
                    .header("X-Signature", sig)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class);
            resp.block();
            log.info("[mail-worker] 发送完成 to={}", to);
        } catch (Exception ex) {
            log.error("[mail-worker] 调用失败 to={} err={}", to, ex.getMessage());
        }
    }

    private void sendViaResend(String from, String to, String subject, String html) {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.error("[mail-resend] api-key 未配置，放弃发送 to={}", to);
            return;
        }
        // Resend 要求 from 格式：Display Name <email@domain> 或 直接 email
        // 若调用方传入的 from 仅是地址且需要显示名，可在外部组装。
        String url = "https://api.resend.com/emails";
        // 纯文本回退：简单去标签截断
        String text = html.replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.length() > 4000) {
            text = text.substring(0, 4000);
        }

        Map<String, Object> payload = Map.of(
                "from", from,
                "to", new String[]{to},
                "subject", subject,
                "html", html,
                "text", text
        );

        String body = JsonUtil.toJson(payload);

        int maxAttempts = 3;
        long backoff = 500L; // 初始 0.5s 指数退避
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                int currentAttempt = attempt;
                String resp = webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + resendApiKey)
                        .bodyValue(body)
                        .retrieve()
                        .onStatus(status -> status.value() >= 400, clientResponse -> {
                            int code = clientResponse.statusCode().value();
                            // 429 / 5xx 触发重试，其它直接失败
                            if ((code == 429 || code >= 500) && currentAttempt < maxAttempts) {
                                return clientResponse.bodyToMono(String.class)
                                        .doOnNext(err -> log.warn("[mail-resend] attempt={} transient status={} body={}", currentAttempt, code, err))
                                        .flatMap(err -> Mono.error(new RuntimeException("TRANSIENT:" + code + ":" + err)));
                            }
                            return clientResponse.bodyToMono(String.class)
                                    .flatMap(err -> Mono.error(new RuntimeException("FATAL:" + code + ":" + err)));
                        })
                        .bodyToMono(String.class)
                        .block();
                log.info("[mail-resend] 发送成功 to={} attempt={} resp={}", to, attempt, resp);
                return;
            } catch (Exception ex) {
                String msg = ex.getMessage();
                boolean transientErr = msg != null && msg.startsWith("TRANSIENT:");
                if (transientErr && attempt < maxAttempts) {
                    try { Thread.sleep(backoff); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                    backoff *= 2; // 指数退避
                    continue;
                }
                log.error("[mail-resend] 发送失败 to={} attempt={} err={}", to, attempt, msg);
                return; // 放弃
            }
        }
    }

    private String hmacSha256Base64(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    /** 简单 JSON 序列化，避免引入额外库（若已用 Jackson 可替换） */
    static class JsonUtil {
        static String toJson(Map<String, Object> map) {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            boolean first = true;
            for (var e : map.entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append('"').append(':');
                Object v = e.getValue();
                if (v == null) sb.append("null");
                else sb.append('"').append(escape(String.valueOf(v))).append('"');
            }
            sb.append('}');
            return sb.toString();
        }
        private static String escape(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }
    }
}
