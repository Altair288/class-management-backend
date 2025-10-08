package com.altair288.class_management.service;

import com.altair288.class_management.model.OperationLog;
import com.altair288.class_management.model.User;
import com.altair288.class_management.repository.OperationLogRepository;
import com.altair288.class_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationLogService {
    private final OperationLogRepository operationLogRepository;
    private final UserRepository userRepository;

    public OperationLogService(OperationLogRepository operationLogRepository,
                               UserRepository userRepository) {
        this.operationLogRepository = operationLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void log(String operation) {
        Integer userId = currentUserId();
        if (userId == null) return; // 忽略匿名
        OperationLog log = new OperationLog();
        log.setUser(new User(userId));
        log.setOperation(operation);
        operationLogRepository.save(log);
    }

    private Integer currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String username = auth.getName();
        return userRepository.findByUsernameOrIdentityNo(username).map(User::getId).orElse(null);
    }
}
