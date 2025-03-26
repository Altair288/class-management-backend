// src/main/java/com/altair288/class_management/config/TestDataInitializer.java
package com.altair288.class_management.config;

import com.altair288.class_management.model.*;
import com.altair288.class_management.repository.*;
import com.altair288.class_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Profile("dev") // 只在开发环境中启用
public class TestDataInitializer implements CommandLineRunner {

    @Autowired private UserService userService;
    @Autowired private RoleService roleService;
    @Autowired private PermissionService permissionService;
    @Autowired private UserRoleService userRoleService;
    @Autowired private RolePermissionService rolePermissionService;

    @Override
    public void run(String... args) {
        // 创建角色
        Role adminRole = new Role(null);
        adminRole.setRoleName(Role.RoleName.ADMIN);
        adminRole = roleService.createRole(adminRole);

        Role teacherRole = new Role(null);
        teacherRole.setRoleName(Role.RoleName.TEACHER);
        teacherRole = roleService.createRole(teacherRole);

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
        adminUser.setPassword("password123"); // 会被加密
        adminUser.setUserType(User.UserType.ADMIN);
        adminUser = userService.registerUser(adminUser);

        User teacherUser = new User(null);
        teacherUser.setUsername("teacher");
        teacherUser.setPassword("password123");
        teacherUser.setUserType(User.UserType.TEACHER);
        teacherUser = userService.registerUser(teacherUser);

        User studentUser = new User(null);
        studentUser.setUsername("student");
        studentUser.setPassword("password123");
        studentUser.setUserType(User.UserType.STUDENT);
        studentUser = userService.registerUser(studentUser);

        // 分配角色
        userRoleService.assignRoleToUser(adminUser.getId(), adminRole.getId());
        userRoleService.assignRoleToUser(teacherUser.getId(), teacherRole.getId());
        userRoleService.assignRoleToUser(studentUser.getId(), studentRole.getId());

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
    }
}