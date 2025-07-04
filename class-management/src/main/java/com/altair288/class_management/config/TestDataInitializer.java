// src/main/java/com/altair288/class_management/config/TestDataInitializer.java
package com.altair288.class_management.config;

import com.altair288.class_management.model.*;
import com.altair288.class_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Lazy;
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
    @Autowired private TeacherService teacherService;
    @Autowired private StudentService studentService;
    @Autowired private ParentService parentService;
    @Autowired private ClassService classService;

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
            
            // 创建教师
            Teacher teacher = new Teacher();
            teacher.setName("张老师");
            teacher.setTeacherNo("T2024001");
            teacher.setPhone("13800000001");
            teacher.setEmail("teacher@example.com");
            teacher = teacherService.save(teacher);
            
            // 再创建班级，并设置教师
            com.altair288.class_management.model.Class clazz1 = new com.altair288.class_management.model.Class();
            clazz1.setName("计算机网络1班");
            clazz1.setTeacher(teacher);
            clazz1 = classService.save(clazz1);

            com.altair288.class_management.model.Class clazz2 = new com.altair288.class_management.model.Class();
            clazz2.setName("计算机网络2班");
            clazz2.setTeacher(teacher);
            clazz2 = classService.save(clazz2);

            com.altair288.class_management.model.Class clazz3 = new com.altair288.class_management.model.Class();
            clazz3.setName("物联网技术1班");
            clazz3.setTeacher(teacher);
            clazz3 = classService.save(clazz3);

            com.altair288.class_management.model.Class clazz4 = new com.altair288.class_management.model.Class();
            clazz4.setName("物联网技术2班");
            clazz4.setTeacher(teacher);
            clazz4 = classService.save(clazz4);

            // 创建学生并设置班级
            Student student1 = new Student();
            student1.setName("王学生");
            student1.setStudentNo("s001");
            student1.setPhone("18915536571");
            student1.setEmail("test@test.com");
            student1.setClazz(clazz1); // 关键：设置class_id
            student1 = studentService.save(student1);

            Student student = new Student();
            student.setName("李学生");
            student.setStudentNo("S2024001");
            student.setPhone("13900000001");
            student.setEmail("student@example.com");
            student.setClazz(clazz1); // 关键：设置class_id
            student = studentService.save(student);

            Parent parent = new Parent();
            parent.setName("王家长");
            parent.setPhone("13700000001");
            parent.setEmail("parent@example.com");
            parent.setStudent(student);
            parent = parentService.save(parent);

            // 创建用户并用学号/工号/手机号作为用户名
            User teacherUser = new User(null);
            teacherUser.setUsername(teacher.getName());
            teacherUser.setIdentityNo(teacher.getTeacherNo());
            teacherUser.setPassword(initialPassword);
            teacherUser.setUserType(User.UserType.TEACHER);
            teacherUser.setRelatedId(teacher.getId());
            teacherUser = userService.registerUser(teacherUser);

            User studentUser1 = new User(null);
            studentUser1.setUsername(student1.getName());
            studentUser1.setIdentityNo(student1.getStudentNo());
            studentUser1.setPassword(initialPassword);
            studentUser1.setUserType(User.UserType.STUDENT);
            studentUser1.setRelatedId(student1.getId());
            studentUser1 = userService.registerUser(studentUser1);

            User studentUser = new User(null);
            studentUser.setUsername(student.getName());
            studentUser.setIdentityNo(student.getStudentNo());
            studentUser.setPassword(initialPassword);
            studentUser.setUserType(User.UserType.STUDENT);
            studentUser.setRelatedId(student.getId());
            studentUser = userService.registerUser(studentUser);

            User parentUser = new User(null);
            parentUser.setUsername(parent.getName());
            parentUser.setIdentityNo(parent.getPhone());
            parentUser.setPassword(initialPassword);
            parentUser.setUserType(User.UserType.PARENT);
            parentUser.setRelatedId(parent.getId());
            parentUser = userService.registerUser(parentUser);

            // 管理员账号可保持原样
            User adminUser = new User(null);
            adminUser.setUsername("admin");
            adminUser.setIdentityNo("admin");
            adminUser.setPassword(initialPassword);
            adminUser.setUserType(User.UserType.ADMIN);
            adminUser = userService.registerUser(adminUser);

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
                    "教师账号：T2024001, 密码：" + initialPassword + "\n" +
                    "学生账号：S2024001, 密码：" + initialPassword + "\n" +
                    "家长账号：Telphone Number , 密码：" + initialPassword);
        } catch (Exception e) {
            logger.error("初始化测试数据时发生错误: ", e);
        }
    }
}