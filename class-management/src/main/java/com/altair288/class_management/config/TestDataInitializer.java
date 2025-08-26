// src/main/java/com/altair288/class_management/config/TestDataInitializer.java
package com.altair288.class_management.config;

import com.altair288.class_management.model.*;
import com.altair288.class_management.dto.CreditItemDTO;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;

@Component
@Profile("dev") // 只在开发环境中启用
public class TestDataInitializer {

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
    @Autowired private StudentCreditService studentCreditService;
    @Autowired private CreditItemRepository creditItemRepository;
    private final CreditItemService creditItemService;

    public TestDataInitializer(UserService userService, RoleService roleService, PermissionService permissionService, UserRoleService userRoleService, RolePermissionService rolePermissionService, TeacherService teacherService, StudentService studentService, ParentService parentService, ClassService classService, StudentCreditService studentCreditService, CreditItemRepository creditItemRepository, CreditItemService creditItemService) {
        this.userService = userService;
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.userRoleService = userRoleService;
        this.rolePermissionService = rolePermissionService;
        this.teacherService = teacherService;
        this.studentService = studentService;
        this.parentService = parentService;
        this.classService = classService;
        this.studentCreditService = studentCreditService;
        this.creditItemRepository = creditItemRepository;
        this.creditItemService = creditItemService;
    }

    @PostConstruct
    public void init() {
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
            clazz1.setName("21计算机网络1班");
            clazz1.setTeacher(teacher);
            clazz1.setGrade("2021");
            clazz1 = classService.save(clazz1);

            com.altair288.class_management.model.Class clazz2 = new com.altair288.class_management.model.Class();
            clazz2.setName("21计算机网络2班");
            clazz2.setTeacher(teacher);
            clazz2.setGrade("2021");
            clazz2 = classService.save(clazz2);

            com.altair288.class_management.model.Class clazz3 = new com.altair288.class_management.model.Class();
            clazz3.setName("21物联网技术1班");
            clazz3.setTeacher(teacher);
            clazz3.setGrade("2021");
            clazz3 = classService.save(clazz3);

            com.altair288.class_management.model.Class clazz4 = new com.altair288.class_management.model.Class();
            clazz4.setName("21物联网技术2班");
            clazz4.setTeacher(teacher);
            clazz4.setGrade("2021");
            clazz4 = classService.save(clazz4);

            com.altair288.class_management.model.Class clazz5 = new com.altair288.class_management.model.Class();
            clazz5.setName("22计算机网络1班");
            clazz5.setTeacher(teacher);
            clazz5.setGrade("2022");
            clazz5 = classService.save(clazz5);

            com.altair288.class_management.model.Class clazz6 = new com.altair288.class_management.model.Class();
            clazz6.setName("22计算机网络2班");
            clazz6.setTeacher(teacher);
            clazz6.setGrade("2022");
            clazz6 = classService.save(clazz6);

            com.altair288.class_management.model.Class clazz7 = new com.altair288.class_management.model.Class();
            clazz7.setName("22物联网技术1班");
            clazz7.setTeacher(teacher);
            clazz7.setGrade("2022");
            clazz7 = classService.save(clazz7);

            com.altair288.class_management.model.Class clazz8 = new com.altair288.class_management.model.Class();
            clazz8.setName("22物联网技术2班");
            clazz8.setTeacher(teacher);
            clazz8.setGrade("2022");
            clazz8 = classService.save(clazz8);

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

            // ====== 学分模块测试数据 ======
            // 额外创建两个班级以匹配前端截图风格
            com.altair288.class_management.model.Class cls2024a = new com.altair288.class_management.model.Class();
            cls2024a.setName("计算机2024-1班");
            cls2024a.setTeacher(teacher);
            cls2024a.setGrade("2024");
            cls2024a = classService.save(cls2024a);

            com.altair288.class_management.model.Class cls2024b = new com.altair288.class_management.model.Class();
            cls2024b.setName("计算机2024-2班");
            cls2024b.setTeacher(teacher);
            cls2024b.setGrade("2024");
            cls2024b = classService.save(cls2024b);

            // 新增示例学生（与截图一致的姓名/学号/班级）
            Student sZhang3 = new Student();
            sZhang3.setName("张三");
            sZhang3.setStudentNo("2024001");
            sZhang3.setClazz(cls2024a);
            sZhang3 = studentService.save(sZhang3);

            Student sLi4 = new Student();
            sLi4.setName("李四");
            sLi4.setStudentNo("2024002");
            sLi4.setClazz(cls2024a);
            sLi4 = studentService.save(sLi4);

            Student sWang5 = new Student();
            sWang5.setName("王五");
            sWang5.setStudentNo("2024003");
            sWang5.setClazz(cls2024b);
            sWang5 = studentService.save(sWang5);

            Student sZhao6 = new Student();
            sZhao6.setName("赵六");
            sZhao6.setStudentNo("2024004");
            sZhao6.setClazz(cls2024b);
            sZhao6 = studentService.save(sZhao6);

            // 创建五个学分配置项（每类仅一个）
            // 若重复运行（dev多次启动），存在则跳过创建
            if (!creditItemRepository.existsByCategory("德")) {
                creditItemService.create(new CreditItemDTO(null, "德", "德育", 60.0, 100.0, true, "思想品德与道德修养"));
            }
            if (!creditItemRepository.existsByCategory("智")) {
                creditItemService.create(new CreditItemDTO(null, "智", "智育", 60.0, 100.0, true, "学业成绩与知识掌握"));
            }
            if (!creditItemRepository.existsByCategory("体")) {
                creditItemService.create(new CreditItemDTO(null, "体", "体育", 60.0, 100.0, true, "身体素质与健康状况"));
            }
            if (!creditItemRepository.existsByCategory("美")) {
                creditItemService.create(new CreditItemDTO(null, "美", "美育", 60.0, 100.0, true, "艺术修养与审美能力"));
            }
            if (!creditItemRepository.existsByCategory("劳")) {
                creditItemService.create(new CreditItemDTO(null, "劳", "劳育", 60.0, 100.0, true, "劳动技能与实践能力"));
            }

            // 获取每个类别的唯一配置项 id
            Integer deId = creditItemRepository.findAllByCategory("德").get(0).getId();
            Integer zhiId = creditItemRepository.findAllByCategory("智").get(0).getId();
            Integer tiId = creditItemRepository.findAllByCategory("体").get(0).getId();
            Integer meiId = creditItemRepository.findAllByCategory("美").get(0).getId();
            Integer laoId = creditItemRepository.findAllByCategory("劳").get(0).getId();

            // 设置每个学生的分值（参考截图示例）
            // 张三：85/92/78/88/85 -> 总分 428 -> excellent
            studentCreditService.setScore(sZhang3.getId(), deId, 85.0);
            studentCreditService.setScore(sZhang3.getId(), zhiId, 92.0);
            studentCreditService.setScore(sZhang3.getId(), tiId, 78.0);
            studentCreditService.setScore(sZhang3.getId(), meiId, 88.0);
            studentCreditService.setScore(sZhang3.getId(), laoId, 85.0);

            // 李四：75/88/82/76/80 -> 401 -> good
            studentCreditService.setScore(sLi4.getId(), deId, 75.0);
            studentCreditService.setScore(sLi4.getId(), zhiId, 88.0);
            studentCreditService.setScore(sLi4.getId(), tiId, 82.0);
            studentCreditService.setScore(sLi4.getId(), meiId, 76.0);
            studentCreditService.setScore(sLi4.getId(), laoId, 80.0);

            // 王五：60/70/65/58/62 -> 315 -> warning
            studentCreditService.setScore(sWang5.getId(), deId, 60.0);
            studentCreditService.setScore(sWang5.getId(), zhiId, 70.0);
            studentCreditService.setScore(sWang5.getId(), tiId, 65.0);
            studentCreditService.setScore(sWang5.getId(), meiId, 58.0);
            studentCreditService.setScore(sWang5.getId(), laoId, 62.0);

            // 赵六：45/55/50/48/52 -> 250 -> danger
            studentCreditService.setScore(sZhao6.getId(), deId, 45.0);
            studentCreditService.setScore(sZhao6.getId(), zhiId, 55.0);
            studentCreditService.setScore(sZhao6.getId(), tiId, 50.0);
            studentCreditService.setScore(sZhao6.getId(), meiId, 48.0);
            studentCreditService.setScore(sZhao6.getId(), laoId, 52.0);

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