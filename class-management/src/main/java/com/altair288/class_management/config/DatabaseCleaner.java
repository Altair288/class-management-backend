package com.altair288.class_management.config;

import com.altair288.class_management.repository.*;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.*;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.transaction.support.*;

@Component
@Profile("dev")
public class DatabaseCleaner implements ApplicationListener<ContextClosedEvent> {
    
    @Autowired private RolePermissionRepository rolePermissionRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PermissionRepository permissionRepository;
    @Autowired private TransactionTemplate transactionTemplate;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        cleanDatabase();
    }

    private void cleanDatabase() {
        transactionTemplate.execute(status -> {
            try {
                // 1. 先删除关联表数据
                rolePermissionRepository.deleteAll();
                userRoleRepository.deleteAll();

                // 2. 删除主表数据
                userRepository.deleteAll();
                roleRepository.deleteAll();
                permissionRepository.deleteAll();
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw new RuntimeException("清理数据库失败", e);
            }
        });
    }
}
