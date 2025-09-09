// src/main/java/com/altair288/class_management/config/TestDataInitializer.java
package com.altair288.class_management.config;

import com.altair288.class_management.model.*;
import com.altair288.class_management.dto.CreditItemDTO;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Date;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.Calendar;

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
    @Autowired private LeaveTypeConfigService leaveTypeConfigService;
    @Autowired private LeaveRequestService leaveRequestService;
    @Autowired private StudentLeaveBalanceService studentLeaveBalanceService;
    @Autowired private com.altair288.class_management.repository.LeaveTypeConfigRepository leaveTypeConfigRepository;
    @Autowired private com.altair288.class_management.repository.DepartmentRepository departmentRepository;
    @Autowired private com.altair288.class_management.repository.RoleAssignmentRepository roleAssignmentRepository;
    private final CreditItemService creditItemService;

    public TestDataInitializer(UserService userService, RoleService roleService, PermissionService permissionService, UserRoleService userRoleService, RolePermissionService rolePermissionService, TeacherService teacherService, StudentService studentService, ParentService parentService, ClassService classService, StudentCreditService studentCreditService, CreditItemRepository creditItemRepository, CreditItemService creditItemService, LeaveTypeConfigService leaveTypeConfigService, LeaveRequestService leaveRequestService, StudentLeaveBalanceService studentLeaveBalanceService) {
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
        this.leaveTypeConfigService = leaveTypeConfigService;
        this.leaveRequestService = leaveRequestService;
        this.studentLeaveBalanceService = studentLeaveBalanceService;
    }

    @PostConstruct
    public void init() {
        try {
            final String initialPassword = "Test@123456";
            // 使用 schema.sql 预置的系统角色 (按 code 查询)
            Role adminRole = roleService.getByCode(Role.Codes.ADMIN);
            Role teacherRole = roleService.getByCode(Role.Codes.TEACHER);
            Role parentRole = roleService.getByCode(Role.Codes.PARENT);
            Role studentRole = roleService.getByCode(Role.Codes.STUDENT);

            // 创建权限
            Permission createUserPermission = new Permission(null);
            createUserPermission.setPermissionName("CREATE_USER");
            createUserPermission.setDescription("创建用户权限");
            createUserPermission = permissionService.createPermission(createUserPermission);

            Permission viewGradesPermission = new Permission(null);
            viewGradesPermission.setPermissionName("VIEW_GRADES");
            viewGradesPermission.setDescription("查看成绩权限");
            viewGradesPermission = permissionService.createPermission(viewGradesPermission);
            
            // 创建教师（共10名）
            Teacher teacher = new Teacher();
            teacher.setName("张老师");
            teacher.setTeacherNo("T2024001");
            teacher.setPhone("138033000001");
            teacher.setEmail("teacher@example.com");
            teacher = teacherService.save(teacher);

            Teacher teacher2 = new Teacher();
            teacher2.setName("李老师");
            teacher2.setTeacherNo("T2024002");
            teacher2.setPhone("138033000002");
            teacher2.setEmail("teacher2@example.com");
            teacher2 = teacherService.save(teacher2);

            Teacher teacher3 = new Teacher();
            teacher3.setName("王老师");
            teacher3.setTeacherNo("T2024003");
            teacher3.setPhone("138033000003");
            teacher3.setEmail("teacher3@example.com");
            teacher3 = teacherService.save(teacher3);

            Teacher teacher4 = new Teacher();
            teacher4.setName("赵老师");
            teacher4.setTeacherNo("T2024004");
            teacher4.setPhone("138033000004");
            teacher4.setEmail("teacher4@example.com");
            teacher4 = teacherService.save(teacher4);

            Teacher teacher5 = new Teacher();
            teacher5.setName("钱老师");
            teacher5.setTeacherNo("T2024005");
            teacher5.setPhone("138033000005");
            teacher5.setEmail("teacher5@example.com");
            teacher5 = teacherService.save(teacher5);

            Teacher teacher6 = new Teacher();
            teacher6.setName("孙老师");
            teacher6.setTeacherNo("T2024006");
            teacher6.setPhone("138033000006");
            teacher6.setEmail("teacher6@example.com");
            teacher6 = teacherService.save(teacher6);

            Teacher teacher7 = new Teacher();
            teacher7.setName("周老师");
            teacher7.setTeacherNo("T2024007");
            teacher7.setPhone("138033000007");
            teacher7.setEmail("teacher7@example.com");
            teacher7 = teacherService.save(teacher7);

            Teacher teacher8 = new Teacher();
            teacher8.setName("吴老师");
            teacher8.setTeacherNo("T2024008");
            teacher8.setPhone("138033000008");
            teacher8.setEmail("teacher8@example.com");
            teacher8 = teacherService.save(teacher8);

            Teacher teacher9 = new Teacher();
            teacher9.setName("郑老师");
            teacher9.setTeacherNo("T2024009");
            teacher9.setPhone("138033000009");
            teacher9.setEmail("teacher9@example.com");
            teacher9 = teacherService.save(teacher9);

            Teacher teacher10 = new Teacher();
            teacher10.setName("冯老师");
            teacher10.setTeacherNo("T2024010");
            teacher10.setPhone("138033000010");
            teacher10.setEmail("teacher10@example.com");
            teacher10 = teacherService.save(teacher10);

            // 额外添加 5 个未分配班级、未配置任何角色指派的教师（用于测试）
            Teacher teacher11 = new Teacher();
            teacher11.setName("测试老师A");
            teacher11.setTeacherNo("T2024011");
            teacher11.setPhone("138033000011");
            teacher11.setEmail("teacher11@example.com");
            teacher11 = teacherService.save(teacher11);

            Teacher teacher12 = new Teacher();
            teacher12.setName("测试老师B");
            teacher12.setTeacherNo("T2024012");
            teacher12.setPhone("138033000012");
            teacher12.setEmail("teacher12@example.com");
            teacher12 = teacherService.save(teacher12);

            Teacher teacher13 = new Teacher();
            teacher13.setName("测试老师C");
            teacher13.setTeacherNo("T2024013");
            teacher13.setPhone("138033000013");
            teacher13.setEmail("teacher13@example.com");
            teacher13 = teacherService.save(teacher13);

            Teacher teacher14 = new Teacher();
            teacher14.setName("测试老师D");
            teacher14.setTeacherNo("T2024014");
            teacher14.setPhone("138033000014");
            teacher14.setEmail("teacher14@example.com");
            teacher14 = teacherService.save(teacher14);

            Teacher teacher15 = new Teacher();
            teacher15.setName("测试老师E");
            teacher15.setTeacherNo("T2024015");
            teacher15.setPhone("138033000015");
            teacher15.setEmail("teacher15@example.com");
            teacher15 = teacherService.save(teacher15);

            Teacher teacher16 = new Teacher();
            teacher16.setName("测试老师F");
            teacher16.setTeacherNo("T2024016");
            teacher16.setPhone("138033000016");
            teacher16.setEmail("teacher16@example.com");
            teacher16 = teacherService.save(teacher16);

            Teacher teacher17 = new Teacher();
            teacher17.setName("测试老师G");
            teacher17.setTeacherNo("T2024017");
            teacher17.setPhone("138033000017");
            teacher17.setEmail("teacher17@example.com");
            teacher17 = teacherService.save(teacher17);

            // 再创建班级，并设置教师
            com.altair288.class_management.model.Class clazz1 = new com.altair288.class_management.model.Class();
            clazz1.setName("21计算机网络1班");
            clazz1.setTeacher(teacher);
            clazz1.setGrade("2021");
            clazz1 = classService.save(clazz1);

            com.altair288.class_management.model.Class clazz2 = new com.altair288.class_management.model.Class();
            clazz2.setName("21计算机网络2班");
            clazz2.setTeacher(teacher2);
            clazz2.setGrade("2021");
            clazz2 = classService.save(clazz2);

            com.altair288.class_management.model.Class clazz3 = new com.altair288.class_management.model.Class();
            clazz3.setName("21物联网技术1班");
            clazz3.setTeacher(teacher3);
            clazz3.setGrade("2021");
            clazz3 = classService.save(clazz3);

            com.altair288.class_management.model.Class clazz4 = new com.altair288.class_management.model.Class();
            clazz4.setName("21物联网技术2班");
            clazz4.setTeacher(teacher4);
            clazz4.setGrade("2021");
            clazz4 = classService.save(clazz4);

            com.altair288.class_management.model.Class clazz5 = new com.altair288.class_management.model.Class();
            clazz5.setName("22计算机网络1班");
            clazz5.setTeacher(teacher5);
            clazz5.setGrade("2022");
            clazz5 = classService.save(clazz5);

            com.altair288.class_management.model.Class clazz6 = new com.altair288.class_management.model.Class();
            clazz6.setName("22计算机网络2班");
            clazz6.setTeacher(teacher6);
            clazz6.setGrade("2022");
            clazz6 = classService.save(clazz6);

            com.altair288.class_management.model.Class clazz7 = new com.altair288.class_management.model.Class();
            clazz7.setName("22物联网技术1班");
            clazz7.setTeacher(teacher7);
            clazz7.setGrade("2022");
            clazz7 = classService.save(clazz7);

            com.altair288.class_management.model.Class clazz8 = new com.altair288.class_management.model.Class();
            clazz8.setName("22物联网技术2班");
            clazz8.setTeacher(teacher8);
            clazz8.setGrade("2022");
            clazz8 = classService.save(clazz8);

            // ====== 系部与角色指派（用于审批链解析）======
            // 1) 创建三个系部（若不存在）：信息系、机电系、服装系
            Department deptInfo = departmentRepository.findByCode("INFO").orElseGet(() -> {
                Department d = new Department();
                d.setName("信息系");
                d.setCode("INFO");
                d.setDescription("信息工程与计算机相关专业");
                d.setEnabled(true);
                return departmentRepository.save(d);
            });
            Department deptMech = departmentRepository.findByCode("MECH").orElseGet(() -> {
                Department d = new Department();
                d.setName("机电系");
                d.setCode("MECH");
                d.setDescription("机械与电气相关专业");
                d.setEnabled(true);
                return departmentRepository.save(d);
            });
            Department deptFash = departmentRepository.findByCode("FASH").orElseGet(() -> {
                Department d = new Department();
                d.setName("服装系");
                d.setCode("FASH");
                d.setDescription("服装与设计相关专业");
                d.setEnabled(true);
                return departmentRepository.save(d);
            });

            // 2) 绑定部分班级到信息系（示例；审批解析将优先使用班级→系部）
            clazz1.setDepartment(deptInfo); classService.save(clazz1);
            clazz2.setDepartment(deptInfo); classService.save(clazz2);
            clazz3.setDepartment(deptMech); classService.save(clazz3);
            clazz4.setDepartment(deptMech); classService.save(clazz4);
            clazz5.setDepartment(deptInfo); classService.save(clazz5);
            clazz6.setDepartment(deptInfo); classService.save(clazz6);
            clazz7.setDepartment(deptFash); classService.save(clazz7);
            clazz8.setDepartment(deptFash); classService.save(clazz8);

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
            Teacher[] teacherArr = {teacher, teacher2, teacher3, teacher4, teacher5, teacher6, teacher7, teacher8, teacher9, teacher10, teacher11, teacher12, teacher13, teacher14, teacher15, teacher16, teacher17};
            java.util.Map<Integer, User> teacherUserMap = new java.util.HashMap<>();
            for (Teacher t : teacherArr) {
                User u = new User(null);
                u.setUsername(t.getName());
                u.setIdentityNo(t.getTeacherNo());
                u.setPassword(initialPassword);
                u.setUserType(User.UserType.TEACHER);
                u.setRelatedId(t.getId());
                u = userService.registerUser(u);
                teacherUserMap.put(t.getId(), u);
            }

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
            // 为所有教师账号分配教师角色
            for (User tu : teacherUserMap.values()) {
                userRoleService.assignRoleToUser(tu.getId(), teacherRole.getId());
            }
            userRoleService.assignRoleToUser(studentUser.getId(), studentRole.getId());
            userRoleService.assignRoleToUser(parentUser.getId(), parentRole.getId()); // 家长分配家长角色

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
            cls2024a.setTeacher(teacher9);
            cls2024a.setGrade("2024");
            cls2024a = classService.save(cls2024a);

            com.altair288.class_management.model.Class cls2024b = new com.altair288.class_management.model.Class();
            cls2024b.setName("计算机2024-2班");
            cls2024b.setTeacher(teacher10);
            cls2024b.setGrade("2024");
            cls2024b = classService.save(cls2024b);

            // 3) 绑定 2024 班级到信息系
            cls2024a.setDepartment(deptInfo); classService.save(cls2024a);
            cls2024b.setDepartment(deptMech); classService.save(cls2024b);

            // 4) 角色指派（层级）：班主任 -> 系部主任 -> 年级主任 -> 校长
            // 绑定审批角色：HOMEROOM（班主任审批角色）
            Role homeroomApprovalRole = roleService.getByCode(Role.Codes.HOMEROOM);
            java.util.function.BiConsumer<com.altair288.class_management.model.Class, Teacher> assignHomeroom = (c,t) -> {
                if (roleAssignmentRepository.findByRoleAndClass(Role.Codes.HOMEROOM, c.getId()).isEmpty()) {
                    RoleAssignment ra = new RoleAssignment();
                    ra.setApprovalRole(homeroomApprovalRole);
                    ra.setTeacherId(t.getId());
                    ra.setClassId(c.getId());
                    ra.setEnabled(true);
                    roleAssignmentRepository.save(ra);
                }
            };
            assignHomeroom.accept(clazz1, teacher);
            assignHomeroom.accept(clazz2, teacher2);
            assignHomeroom.accept(clazz3, teacher3);
            assignHomeroom.accept(clazz4, teacher4);
            assignHomeroom.accept(clazz5, teacher5);
            assignHomeroom.accept(clazz6, teacher6);
            assignHomeroom.accept(clazz7, teacher7);
            assignHomeroom.accept(clazz8, teacher8);
            assignHomeroom.accept(cls2024a, teacher9);
            assignHomeroom.accept(cls2024b, teacher10);

            // 移除额外的系部主任、年级主任、校长等角色指派；仅保留班主任。

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

            // ====== 请假管理系统测试数据 ======
            
            // 1. 创建请假类型配置 (检查是否已存在)
            LeaveTypeConfig sickLeaveConfig = leaveTypeConfigRepository.findByTypeCode("sick");
            if (sickLeaveConfig == null) {
                sickLeaveConfig = new LeaveTypeConfig();
                sickLeaveConfig.setTypeCode("sick");
                sickLeaveConfig.setTypeName("病假");
                sickLeaveConfig.setMaxDaysPerRequest(90);
                sickLeaveConfig.setAnnualAllowance(10);
                sickLeaveConfig.setAdvanceDaysRequired(1);
                sickLeaveConfig.setRequiresApproval(false);
                sickLeaveConfig.setRequiresMedicalProof(true);
                sickLeaveConfig.setEnabled(true);
                sickLeaveConfig.setColor("#388e3c");
                sickLeaveConfig.setDescription("因病需要休息，需提供医疗证明");
                sickLeaveConfig = leaveTypeConfigService.saveLeaveType(sickLeaveConfig);
            }

            LeaveTypeConfig personalLeaveConfig = leaveTypeConfigRepository.findByTypeCode("personal");
            if (personalLeaveConfig == null) {
                personalLeaveConfig = new LeaveTypeConfig();
                personalLeaveConfig.setTypeCode("personal");
                personalLeaveConfig.setTypeName("事假");
                personalLeaveConfig.setMaxDaysPerRequest(10);
                personalLeaveConfig.setAnnualAllowance(5);
                personalLeaveConfig.setAdvanceDaysRequired(1);
                personalLeaveConfig.setRequiresApproval(true);
                personalLeaveConfig.setRequiresMedicalProof(false);
                personalLeaveConfig.setEnabled(true);
                personalLeaveConfig.setColor("#f57c00");
                personalLeaveConfig.setDescription("因个人事务需要请假");
                personalLeaveConfig = leaveTypeConfigService.saveLeaveType(personalLeaveConfig);
            }

            LeaveTypeConfig annualLeaveConfig = leaveTypeConfigRepository.findByTypeCode("annual");
            if (annualLeaveConfig == null) {
                annualLeaveConfig = new LeaveTypeConfig();
                annualLeaveConfig.setTypeCode("annual");
                annualLeaveConfig.setTypeName("年假");
                annualLeaveConfig.setMaxDaysPerRequest(30);
                annualLeaveConfig.setAnnualAllowance(15);
                annualLeaveConfig.setAdvanceDaysRequired(3);
                annualLeaveConfig.setRequiresApproval(true);
                annualLeaveConfig.setRequiresMedicalProof(false);
                annualLeaveConfig.setEnabled(true);
                annualLeaveConfig.setColor("#1976d2");
                annualLeaveConfig.setDescription("每年享有的带薪年假，需提前申请");
                annualLeaveConfig = leaveTypeConfigService.saveLeaveType(annualLeaveConfig);
            }

            LeaveTypeConfig maternityLeaveConfig = leaveTypeConfigRepository.findByTypeCode("maternity");
            if (maternityLeaveConfig == null) {
                maternityLeaveConfig = new LeaveTypeConfig();
                maternityLeaveConfig.setTypeCode("maternity");
                maternityLeaveConfig.setTypeName("产假");
                maternityLeaveConfig.setMaxDaysPerRequest(128);
                maternityLeaveConfig.setAnnualAllowance(128);
                maternityLeaveConfig.setAdvanceDaysRequired(30);
                maternityLeaveConfig.setRequiresApproval(true);
                maternityLeaveConfig.setRequiresMedicalProof(true);
                maternityLeaveConfig.setEnabled(true);
                maternityLeaveConfig.setColor("#e91e63");
                maternityLeaveConfig.setDescription("女性员工生育期间的带薪假期");
                maternityLeaveConfig = leaveTypeConfigService.saveLeaveType(maternityLeaveConfig);
            }

            // 2. 初始化学生请假余额 - 使用2024学年
            Integer currentYear = 2025;
            studentLeaveBalanceService.initializeStudentBalance(sZhang3.getId(), sickLeaveConfig.getId(), currentYear, sickLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sZhang3.getId(), personalLeaveConfig.getId(), currentYear, personalLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sZhang3.getId(), annualLeaveConfig.getId(), currentYear, annualLeaveConfig.getAnnualAllowance());

            studentLeaveBalanceService.initializeStudentBalance(sLi4.getId(), sickLeaveConfig.getId(), currentYear, sickLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sLi4.getId(), personalLeaveConfig.getId(), currentYear, personalLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sLi4.getId(), annualLeaveConfig.getId(), currentYear, annualLeaveConfig.getAnnualAllowance());

            studentLeaveBalanceService.initializeStudentBalance(sWang5.getId(), sickLeaveConfig.getId(), currentYear, sickLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sWang5.getId(), personalLeaveConfig.getId(), currentYear, personalLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sWang5.getId(), annualLeaveConfig.getId(), currentYear, annualLeaveConfig.getAnnualAllowance());

            studentLeaveBalanceService.initializeStudentBalance(sZhao6.getId(), sickLeaveConfig.getId(), currentYear, sickLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sZhao6.getId(), personalLeaveConfig.getId(), currentYear, personalLeaveConfig.getAnnualAllowance());
            studentLeaveBalanceService.initializeStudentBalance(sZhao6.getId(), annualLeaveConfig.getId(), currentYear, annualLeaveConfig.getAnnualAllowance());

            // 为王学生、李学生补齐所有启用类型的余额（同一年份）
            studentLeaveBalanceService.initializeBalancesForStudentAllEnabled(student1.getId(), currentYear);
            studentLeaveBalanceService.initializeBalancesForStudentAllEnabled(student.getId(), currentYear);

            // 旧初始化. 创建一些示例请假申请

            // // 张三的病假申请（已批准）
            // LeaveRequest sickLeave1 = new LeaveRequest();
            // sickLeave1.setStudentId(sZhang3.getId());
            // sickLeave1.setLeaveTypeId(sickLeaveConfig.getId());
            // sickLeave1.setStartDate(new Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L)); // 5天前
            // sickLeave1.setEndDate(new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L)); // 3天前
            // sickLeave1.setDays(2.0);
            // sickLeave1.setReason("感冒发烧需要休息");
            // // 不直接设状态，提交后再调用批准逻辑
            // sickLeave1.setCreatedAt(new Date(System.currentTimeMillis() - 6 * 24 * 60 * 60 * 1000L)); // 6天前提交
            // sickLeave1.setEmergencyContact("张父亲");
            // sickLeave1.setEmergencyPhone("13800138001");
            // sickLeave1 = leaveRequestService.submitLeaveRequest(sickLeave1);
            // leaveRequestService.approveLeaveRequest(sickLeave1.getId(), teacher.getId(), "初始化批准");

            // // 李四的事假申请（待审批）
            // LeaveRequest personalLeave1 = new LeaveRequest();
            // personalLeave1.setStudentId(sLi4.getId());
            // personalLeave1.setLeaveTypeId(personalLeaveConfig.getId());
            // personalLeave1.setStartDate(new Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L)); // 3天后
            // personalLeave1.setEndDate(new Date(System.currentTimeMillis() + 4 * 24 * 60 * 60 * 1000L)); // 4天后
            // personalLeave1.setDays(1.0);
            // personalLeave1.setReason("家庭事务处理");
            // // 待审批：仅提交，不直接写状态，由服务默认置为待审批
            // personalLeave1.setCreatedAt(new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000L)); // 2小时前提交
            // personalLeave1.setEmergencyContact("李母亲");
            // personalLeave1.setEmergencyPhone("13800138002");
            // personalLeave1 = leaveRequestService.submitLeaveRequest(personalLeave1);

            // // 王五的年假申请（已批准）
            // LeaveRequest annualLeave1 = new LeaveRequest();
            // annualLeave1.setStudentId(sWang5.getId());
            // annualLeave1.setLeaveTypeId(annualLeaveConfig.getId());
            // annualLeave1.setStartDate(new Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L)); // 10天后
            // annualLeave1.setEndDate(new Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000L)); // 14天后
            // annualLeave1.setDays(5.0);
            // annualLeave1.setReason("年假旅游");
            // // 不直接设状态，提交后再调用批准逻辑
            // annualLeave1.setCreatedAt(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L)); // 7天前提交
            // annualLeave1.setEmergencyContact("王配偶");
            // annualLeave1.setEmergencyPhone("13800138003");
            // annualLeave1 = leaveRequestService.submitLeaveRequest(annualLeave1);
            // leaveRequestService.approveLeaveRequest(annualLeave1.getId(), teacher.getId(), "初始化批准");

            // // 赵六的病假申请（被拒绝）
            // LeaveRequest sickLeave2 = new LeaveRequest();
            // sickLeave2.setStudentId(sZhao6.getId());
            // sickLeave2.setLeaveTypeId(sickLeaveConfig.getId());
            // sickLeave2.setStartDate(new Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L)); // 2天前
            // sickLeave2.setEndDate(new Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000L)); // 1天前
            // sickLeave2.setDays(1.0);
            // sickLeave2.setReason("身体不适");
            // // 不直接设状态，提交后再调用拒绝逻辑
            // sickLeave2.setCreatedAt(new Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L)); // 3天前提交
            // sickLeave2.setEmergencyContact("赵父亲");
            // sickLeave2.setEmergencyPhone("13800138004");
            // sickLeave2 = leaveRequestService.submitLeaveRequest(sickLeave2);
            // leaveRequestService.rejectLeaveRequest(sickLeave2.getId(), teacher.getId(), "初始化拒绝");

            // // 张三的紧急事假申请（待审批）
            // LeaveRequest emergencyLeave = new LeaveRequest();
            // emergencyLeave.setStudentId(sZhang3.getId());
            // emergencyLeave.setLeaveTypeId(personalLeaveConfig.getId());
            // emergencyLeave.setStartDate(new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000L)); // 2小时后
            // emergencyLeave.setEndDate(new Date(System.currentTimeMillis() + 8 * 60 * 60 * 1000L)); // 8小时后
            // emergencyLeave.setDays(1.0); // 半天按1天计算
            // emergencyLeave.setReason("家庭紧急情况");
            // // 待审批：仅提交，不直接写状态，由服务默认置为待审批
            // emergencyLeave.setCreatedAt(new Date(System.currentTimeMillis() - 30 * 60 * 1000L)); // 30分钟前提交
            // emergencyLeave.setEmergencyContact("张父亲");
            // emergencyLeave.setEmergencyPhone("13800138001");
            // emergencyLeave.setHandoverNotes("已安排同学代课");
            // emergencyLeave = leaveRequestService.submitLeaveRequest(emergencyLeave);

            // ====== 追加：6-8月的 10 条示例请假（2025 年）======
            // 帮助方法：构造 yyyy-MM-dd 的 Date
            java.util.function.BiFunction<Integer, int[], Date> makeDate = (year, ymd) -> {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.YEAR, year);
                c.set(Calendar.MONTH, ymd[0] - 1);
                c.set(Calendar.DAY_OF_MONTH, ymd[1]);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                return c.getTime();
            };

            // 1) 张三 病假 2025-06-03 ~ 2025-06-05 批准
            LeaveRequest r1 = new LeaveRequest();
            r1.setStudentId(sZhang3.getId());
            r1.setLeaveTypeId(sickLeaveConfig.getId());
            r1.setStartDate(makeDate.apply(2025, new int[]{6,3}));
            r1.setEndDate(makeDate.apply(2025, new int[]{6,5}));
            r1.setReason("初始化样例：张三病假");
            r1 = leaveRequestService.submitLeaveRequest(r1);
            leaveRequestService.approveLeaveRequest(r1.getId(), teacher.getId(), "示例批准");

            // 2) 李四 事假 2025-06-10 ~ 2025-06-10 待审批
            LeaveRequest r2 = new LeaveRequest();
            r2.setStudentId(sLi4.getId());
            r2.setLeaveTypeId(personalLeaveConfig.getId());
            r2.setStartDate(makeDate.apply(2025, new int[]{6,10}));
            r2.setEndDate(makeDate.apply(2025, new int[]{6,10}));
            r2.setReason("初始化样例：李四事假");
            r2 = leaveRequestService.submitLeaveRequest(r2);

            // 3) 王五 年假 2025-06-15 ~ 2025-06-18 批准
            LeaveRequest r3 = new LeaveRequest();
            r3.setStudentId(sWang5.getId());
            r3.setLeaveTypeId(annualLeaveConfig.getId());
            r3.setStartDate(makeDate.apply(2025, new int[]{6,15}));
            r3.setEndDate(makeDate.apply(2025, new int[]{6,18}));
            r3.setReason("初始化样例：王五年假");
            r3 = leaveRequestService.submitLeaveRequest(r3);
            leaveRequestService.approveLeaveRequest(r3.getId(), teacher.getId(), "示例批准");

            // 4) 赵六 病假 2025-06-20 ~ 2025-06-21 拒绝
            LeaveRequest r4 = new LeaveRequest();
            r4.setStudentId(sZhao6.getId());
            r4.setLeaveTypeId(sickLeaveConfig.getId());
            r4.setStartDate(makeDate.apply(2025, new int[]{6,20}));
            r4.setEndDate(makeDate.apply(2025, new int[]{6,21}));
            r4.setReason("初始化样例：赵六病假");
            r4 = leaveRequestService.submitLeaveRequest(r4);
            leaveRequestService.rejectLeaveRequest(r4.getId(), teacher.getId(), "示例拒绝");

            // 5) 张三 事假 2025-07-01 ~ 2025-07-02 批准
            LeaveRequest r5 = new LeaveRequest();
            r5.setStudentId(sZhang3.getId());
            r5.setLeaveTypeId(personalLeaveConfig.getId());
            r5.setStartDate(makeDate.apply(2025, new int[]{7,1}));
            r5.setEndDate(makeDate.apply(2025, new int[]{7,2}));
            r5.setReason("初始化样例：张三事假");
            r5 = leaveRequestService.submitLeaveRequest(r5);
            leaveRequestService.approveLeaveRequest(r5.getId(), teacher.getId(), "示例批准");

            // 6) 李四 病假 2025-07-05 ~ 2025-07-07 批准
            LeaveRequest r6 = new LeaveRequest();
            r6.setStudentId(sLi4.getId());
            r6.setLeaveTypeId(sickLeaveConfig.getId());
            r6.setStartDate(makeDate.apply(2025, new int[]{7,5}));
            r6.setEndDate(makeDate.apply(2025, new int[]{7,7}));
            r6.setReason("初始化样例：李四病假");
            r6 = leaveRequestService.submitLeaveRequest(r6);
            leaveRequestService.approveLeaveRequest(r6.getId(), teacher.getId(), "示例批准");

            // 7) 王五 事假 2025-07-12 ~ 2025-07-12 待审批
            LeaveRequest r7 = new LeaveRequest();
            r7.setStudentId(sWang5.getId());
            r7.setLeaveTypeId(personalLeaveConfig.getId());
            r7.setStartDate(makeDate.apply(2025, new int[]{7,12}));
            r7.setEndDate(makeDate.apply(2025, new int[]{7,12}));
            r7.setReason("初始化样例：王五事假");
            r7 = leaveRequestService.submitLeaveRequest(r7);

            // 8) 赵六 年假 2025-08-03 ~ 2025-08-06 批准
            LeaveRequest r8 = new LeaveRequest();
            r8.setStudentId(sZhao6.getId());
            r8.setLeaveTypeId(annualLeaveConfig.getId());
            r8.setStartDate(makeDate.apply(2025, new int[]{8,3}));
            r8.setEndDate(makeDate.apply(2025, new int[]{8,6}));
            r8.setReason("初始化样例：赵六年假");
            r8 = leaveRequestService.submitLeaveRequest(r8);
            leaveRequestService.approveLeaveRequest(r8.getId(), teacher.getId(), "示例批准");

            // 9) 张三 病假 2025-08-15 ~ 2025-08-16 待审批
            LeaveRequest r9 = new LeaveRequest();
            r9.setStudentId(sZhang3.getId());
            r9.setLeaveTypeId(sickLeaveConfig.getId());
            r9.setStartDate(makeDate.apply(2025, new int[]{8,15}));
            r9.setEndDate(makeDate.apply(2025, new int[]{8,16}));
            r9.setReason("初始化样例：张三病假(待审批)");
            r9 = leaveRequestService.submitLeaveRequest(r9);

            // 10) 李四 年假 2025-08-20 ~ 2025-08-25 拒绝
            LeaveRequest r10 = new LeaveRequest();
            r10.setStudentId(sLi4.getId());
            r10.setLeaveTypeId(annualLeaveConfig.getId());
            r10.setStartDate(makeDate.apply(2025, new int[]{8,20}));
            r10.setEndDate(makeDate.apply(2025, new int[]{8,25}));
            r10.setReason("初始化样例：李四年假");
            r10 = leaveRequestService.submitLeaveRequest(r10);
            leaveRequestService.rejectLeaveRequest(r10.getId(), teacher.getId(), "示例拒绝");

            logger.info("测试数据初始化完成,请使用用户名和密码登录：\n" +
                    "管理员账号：admin, 密码：" + initialPassword + "\n" +
                    "教师账号：T2024001, 密码：" + initialPassword + "\n" +
                    "学生账号：S2024001, 密码：" + initialPassword + "\n" +
                    "家长账号：Telphone Number , 密码：" + initialPassword + "\n" +
                    "请假管理测试数据已创建，包括4种请假类型和5个示例申请");
        } catch (Exception e) {
            logger.error("初始化测试数据时发生错误: ", e);
        }
    }
}