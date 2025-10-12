// src/main/java/com/altair288/class_management/config/SecurityConfig.java
package com.altair288.class_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // @Bean
    // public UserDetailsService userDetailsService(CustomUserDetailsService customUserDetailsService) {
    //     return customUserDetailsService;
    // }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    AuthenticationEntryPoint sseEntryPoint = new SseAuthenticationEntryPoint();
    SseAccessDeniedHandler sseDeniedHandler = new SseAccessDeniedHandler();
    boolean verbose = Boolean.parseBoolean(System.getProperty("security.debug.accessDenied", "true"));
    boolean verboseStack = Boolean.parseBoolean(System.getProperty("security.debug.accessDenied.stack", "false"));
    LoggingAccessDeniedHandler loggingHandler = new LoggingAccessDeniedHandler(sseDeniedHandler, verbose, verboseStack);
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                // Spring Boot 默认错误转发路径，必须放行，否则未认证 -> 触发 /error -> 再次鉴权失败形成递归日志
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/users/register/student").permitAll()
                .requestMatchers("/api/users/register/parent").permitAll()
                .requestMatchers("/api/users/register/teacher").permitAll()
                .requestMatchers("/api/users/login").permitAll()
                .requestMatchers("/api/users/classes").permitAll()
                // SSE: 改为放行，内部控制器自行校验，以避免异步 dispatch 阶段的 AuthorizationDeniedException 噪声
                .requestMatchers("/api/notifications/stream").permitAll()
                .requestMatchers("/api/leave/current-user-info").authenticated()
                // .requestMatchers("/api/credits/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(sseEntryPoint)
                .accessDeniedHandler(loggingHandler)
            )
                .formLogin(form -> form
                .loginProcessingUrl("/api/users/login")
                .successHandler((req, res, auth) -> {
                    // 强制创建 Session 以便下发 JSESSIONID
                    req.getSession(true);
                    if (auth != null) { /* 引用 auth 避免未使用告警 */ }
                    // 避免再设置任何可能含非 ASCII 的自定义 Header （Tomcat 对 0-255 以外字符直接剥离）。
                    // 如果确实需要传递显示名：可改为 Base64 / URL 编码再由前端解码，这里直接不传，以确保无 WARNING。
                    res.setHeader("X-Path", req.getRequestURI());
                    res.setHeader("X-Login", "OK");
                    res.setStatus(200);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"message\":\"登录成功\"}");
                })
                .failureHandler((req, res, ex) -> {
                    if (ex != null) {
                        res.setHeader("X-Error-Type", ex.getClass().getSimpleName());
                    }
                    res.setHeader("X-Path", req.getRequestURI());
                    res.setStatus(401);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"message\": \"用户名和密码错误\"}");
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/api/users/logout")
                .logoutSuccessHandler((req, res, auth) -> {
                    res.setHeader("X-Path", req.getRequestURI());
                    // 不再写入可能含非 ASCII 的名字到 Header，改为写入 JSON Body
                    String displayName = auth != null ? auth.getName() : "";
                    Map<String, Object> body = new HashMap<>();
                    body.put("message", "登出成功");
                    body.put("displayName", displayName);
                    res.setStatus(200);
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write(new ObjectMapper().writeValueAsString(body));
                })
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 使用模式，覆盖 localhost 与常见私有网段，便于手机通过局域网 IP 访问
        config.setAllowedOriginPatterns(List.of(
            "https://arch.altair288.eu.org:*",
            "http://arch.altair288.eu.org:*",
            "http://localhost:*",
            "http://127.0.0.1:*",
            "http://10.*:*",
            "http://172.16.*:*",
            "http://172.17.*:*",
            "http://172.18.*:*",
            "http://172.19.*:*",
            "http://172.20.*:*",
            "http://172.21.*:*",
            "http://172.22.*:*",
            "http://172.23.*:*",
            "http://172.24.*:*",
            "http://172.25.*:*",
            "http://172.26.*:*",
            "http://172.27.*:*",
            "http://172.28.*:*",
            "http://172.29.*:*",
            "http://172.30.*:*",
            "http://172.31.*:*",
            "http://192.168.*:*"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization","Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}