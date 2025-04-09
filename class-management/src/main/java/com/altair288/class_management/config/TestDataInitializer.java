// src/main/java/com/altair288/class_management/config/TestDataInitializer.java
package com.altair288.class_management.config;

import com.altair288.class_management.model.*;
import com.altair288.class_management.repository.*;
import com.altair288.class_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Profile("dev") // 只在开发环境中启用
public class TestDataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestDataInitializer.class);

    @Autowired private UserService userService;
    @Autowired private RoleService roleService;
    @Autowired private PermissionService permissionService;
    @Autowired private UserRoleService userRoleService;
    @Autowired private RolePermissionService rolePermissionService;

    @Override
    public void run(String... args) {
        try {
            final String initialPassword = "Test@123456";
            // 创建角色
            Role adminRole = new Role(null);
            adminRole.setRoleName(Role.RoleName.ADMIN);
            adminRole = roleService.createRole(adminRole);

            Role teacherRole = new Role(null);
            teacherRole.setRoleName(Role.RoleName.TEACHER);
            teacherRole = roleService.createRole(teacherRole);

            Role parentRole = new Role(null);
            parentRole.setRoleName(Role.RoleName.PARENT);
            parentRole = roleService.createRole(parentRole);

            Role studentRole = new Role(null);
            studentRole.setRoleName(Role.RoleName.STUDENT);
            studentRole = roleService.createRole(studentRole);

            // 创建权限
            Permission createUserPermission = new Permission(null);
            createUserPermission.setPermissionName("CREATE_USER");
            createUserPermission.setDescription("创建用户权限");
            createUserPermission = permissionService.createPermission(createUserPermission);

            Permission viewGradesPermission = new Permission(null);
            viewGradesPermission.setPermissionName("VIEW_GRADES");
            viewGradesPermission.setDescription("查看成绩权限");
            viewGradesPermission = permissionService.createPermission(viewGradesPermission);

            // 创建用户
            User adminUser = new User(null);
            adminUser.setUsername("admin");
            adminUser.setPassword(initialPassword); // 会被加密
            adminUser.setUserType(User.UserType.ADMIN);
            adminUser = userService.registerUser(adminUser);

            User teacherUser = new User(null);
            teacherUser.setUsername("teacher");
            teacherUser.setPassword(initialPassword);
            teacherUser.setUserType(User.UserType.TEACHER);
            teacherUser = userService.registerUser(teacherUser);

            User parentUser = new User(null);
            parentUser.setUsername("parent");
            parentUser.setPassword(initialPassword);
            parentUser.setUserType(User.UserType.PARENT);
            parentUser = userService.registerUser(parentUser);

            User studentUser = new User(null);
            studentUser.setUsername("student");
            studentUser.setPassword(initialPassword);
            studentUser.setUserType(User.UserType.STUDENT);
            studentUser = userService.registerUser(studentUser);

            // 分配角色
            userRoleService.assignRoleToUser(adminUser.getId(), adminRole.getId());
            userRoleService.assignRoleToUser(teacherUser.getId(), teacherRole.getId());
            userRoleService.assignRoleToUser(studentUser.getId(), studentRole.getId());
            userRoleService.assignRoleToUser(parentUser.getId(), parentUser.getId()); // 家长也可以是学生角色

            // 分配权限
            RolePermission adminCreateUser = new RolePermission();
            adminCreateUser.setRole(adminRole);
            adminCreateUser.setPermission(createUserPermission);
            adminCreateUser.setGrantedBy(adminUser);
            rolePermissionService.assignPermissionToRole(adminCreateUser);

            RolePermission teacherViewGrades = new RolePermission();
            teacherViewGrades.setRole(teacherRole);
            teacherViewGrades.setPermission(viewGradesPermission);
            teacherViewGrades.setGrantedBy(adminUser);
            rolePermissionService.assignPermissionToRole(teacherViewGrades);

            logger.info("测试数据初始化完成,请使用用户名和密码登录：\n" +
                    "管理员账号：admin, 密码：" + initialPassword + "\n" +
                    "教师账号：teacher, 密码：" + initialPassword + "\n" +
                    "学生账号：student, 密码：" + initialPassword + "\n" +
                    "家长账号：parent, 密码：" + initialPassword);
        } catch (Exception e) {
            logger.error("初始化测试数据时发生错误: ", e);
        }
    }
}