// package com.altair288.class_management.config;

// import com.altair288.class_management.repository.*;
// import jakarta.annotation.PreDestroy;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
// import org.springframework.context.ApplicationListener;
// import org.springframework.context.annotation.*;
// import org.springframework.context.event.ContextClosedEvent;
// import org.springframework.transaction.support.*;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

// @Component
// @Profile("dev")
// public class DatabaseCleaner implements ApplicationListener<ContextClosedEvent> {

//     private static final Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);

//     @Autowired private RolePermissionRepository rolePermissionRepository;
//     @Autowired private UserRoleRepository userRoleRepository;
//     @Autowired private UserRepository userRepository;
//     @Autowired private RoleRepository roleRepository;
//     @Autowired private PermissionRepository permissionRepository;
//     @Autowired private TransactionTemplate transactionTemplate;

//     @Override
//     public void onApplicationEvent(ContextClosedEvent event) {
//         cleanDatabase();
//     }

//     private void cleanDatabase() {
//         transactionTemplate.execute(status -> {
//             try {
//                 logger.info("开始清理数据库...");

//                 // 1. 先删除关联表数据
//                 rolePermissionRepository.deleteAll();
//                 logger.info("已清空 RolePermission 表数据");

//                 userRoleRepository.deleteAll();
//                 logger.info("已清空 UserRole 表数据");

//                 // 2. 删除主表数据
//                 userRepository.deleteAll();
//                 logger.info("已清空 User 表数据");

//                 roleRepository.deleteAll();
//                 logger.info("已清空 Role 表数据");

//                 permissionRepository.deleteAll();
//                 logger.info("已清空 Permission 表数据");

//                 logger.info("数据库清理完成");
//                 return true;
//             } catch (Exception e) {
//                 logger.error("清理数据库时发生错误: ", e);
//                 status.setRollbackOnly();
//                 throw new RuntimeException("清理数据库失败", e);
//             }
//         });
//     }
// }
