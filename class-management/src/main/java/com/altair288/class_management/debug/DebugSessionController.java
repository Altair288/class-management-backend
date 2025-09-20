package com.altair288.class_management.debug;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugSessionController {

    @GetMapping("/session")
    public Map<String,Object> session(HttpServletRequest request) {
        Map<String,Object> m = new LinkedHashMap<>();
        var session = request.getSession(false);
        m.put("sessionExists", session != null);
        m.put("sessionId", session == null? null: session.getId());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        m.put("principal", auth == null? null: auth.getName());
        m.put("authClass", auth == null? null: auth.getClass().getName());
        m.put("authenticated", auth != null && auth.isAuthenticated());
        return m;
    }
}
