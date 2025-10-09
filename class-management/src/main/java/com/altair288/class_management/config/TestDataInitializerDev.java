package com.altair288.class_management.config;

import com.altair288.class_management.model.*;
import com.altair288.class_management.dto.CreditItemDTO;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

import com.altair288.class_management.repository.UserRepository;

/**
 * DEV 环境演示数据初始化（从原 TestDataInitializer 重命名）。
 * 注意：结构由 Flyway 管理；此类只补充演示/测试数据。
 * 幂等策略：
 *  1) 通过哨兵(admin 用户) 判断是否已初始化；存在则整体跳过，避免重复插入导致唯一约束冲突。
 *  2) 若未来需要“增量补齐”，可改为 ensureXxx() 形式逐项判断。
 */
@Component
@Profile("dev")
public class TestDataInitializerDev {
    private static final Logger logger = LoggerFactory.getLogger(TestDataInitializerDev.class);

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
    @Autowired private CreditItemService creditItemService;
    @Autowired private LeaveTypeConfigService leaveTypeConfigService;
    @Autowired private StudentLeaveBalanceService studentLeaveBalanceService;
    @Autowired private com.altair288.class_management.repository.LeaveTypeConfigRepository leaveTypeConfigRepository;
    @Autowired private com.altair288.class_management.repository.DepartmentRepository departmentRepository;
    @Autowired private com.altair288.class_management.repository.RoleAssignmentRepository roleAssignmentRepository;
    @Autowired private UserRepository userRepository; // 用于幂等检测

    @PostConstruct
    public void init() {
        // 1. 哨兵检测：若 admin 用户存在则判定已初始化，直接跳过
        try {
            if (userRepository.existsByUsername("admin")) {
                logger.info("[TestDataInitializerDev] 检测到 admin 用户已存在，视为演示数据已初始化，跳过后续创建。若需重置请清空数据库或删除相关记录。");
                return;
            }
        } catch (Exception e) {
            // 如果检查异常（极少数情况），仍然继续，避免因检测失败阻塞初始化
            logger.warn("[TestDataInitializerDev] 初始化前检测异常，继续执行初始化: {}", e.getMessage());
        }

        try {
            final String initialPassword = "Test@123456";
            Role adminRole = roleService.getByCode(Role.Codes.ADMIN);
            Role teacherRole = roleService.getByCode(Role.Codes.TEACHER);
            Role parentRole = roleService.getByCode(Role.Codes.PARENT);
            Role studentRole = roleService.getByCode(Role.Codes.STUDENT);

            Permission createUserPermission = new Permission(null);
            createUserPermission.setPermissionName("CREATE_USER");
            createUserPermission.setDescription("创建用户权限");
            createUserPermission = permissionService.createPermission(createUserPermission);

            Permission viewGradesPermission = new Permission(null);
            viewGradesPermission.setPermissionName("VIEW_GRADES");
            viewGradesPermission.setDescription("查看成绩权限");
            viewGradesPermission = permissionService.createPermission(viewGradesPermission);

            Teacher teacher = mkTeacher("张老师", "T2024001", "138033000001", "teacher@example.com");
            Teacher teacher2 = mkTeacher("李老师", "T2024002", "138033000002", "teacher2@example.com");
            Teacher teacher3 = mkTeacher("王老师", "T2024003", "138033000003", "teacher3@example.com");
            Teacher teacher4 = mkTeacher("赵老师", "T2024004", "138033000004", "teacher4@example.com");
            Teacher teacher5 = mkTeacher("钱老师", "T2024005", "138033000005", "teacher5@example.com");
            Teacher teacher6 = mkTeacher("孙老师", "T2024006", "138033000006", "teacher6@example.com");
            Teacher teacher7 = mkTeacher("周老师", "T2024007", "138033000007", "teacher7@example.com");
            Teacher teacher8 = mkTeacher("吴老师", "T2024008", "138033000008", "teacher8@example.com");
            Teacher teacher9 = mkTeacher("郑老师", "T2024009", "138033000009", "teacher9@example.com");
            Teacher teacher10 = mkTeacher("冯老师", "T2024010", "138033000010", "teacher10@example.com");

            Teacher teacher11 = mkTeacher("测试老师A", "T2024011", "138033000011", "teacher11@example.com");
            Teacher teacher12 = mkTeacher("测试老师B", "T2024012", "138033000012", "teacher12@example.com");
            Teacher teacher13 = mkTeacher("测试老师C", "T2024013", "138033000013", "teacher13@example.com");
            Teacher teacher14 = mkTeacher("测试老师D", "T2024014", "138033000014", "teacher14@example.com");
            Teacher teacher15 = mkTeacher("测试老师E", "T2024015", "138033000015", "teacher15@example.com");
            Teacher teacher16 = mkTeacher("测试老师F", "T2024016", "138033000016", "teacher16@example.com");
            Teacher teacher17 = mkTeacher("测试老师G", "T2024017", "138033000017", "teacher17@example.com");

            com.altair288.class_management.model.Class clazz1 = mkClass("21计算机网络1班", teacher, "2021");
            com.altair288.class_management.model.Class clazz2 = mkClass("21计算机网络2班", teacher2, "2021");
            com.altair288.class_management.model.Class clazz3 = mkClass("21物联网技术1班", teacher3, "2021");
            com.altair288.class_management.model.Class clazz4 = mkClass("21物联网技术2班", teacher4, "2021");
            com.altair288.class_management.model.Class clazz5 = mkClass("22计算机网络1班", teacher5, "2022");
            com.altair288.class_management.model.Class clazz6 = mkClass("22计算机网络2班", teacher6, "2022");
            com.altair288.class_management.model.Class clazz7 = mkClass("22物联网技术1班", teacher7, "2022");
            com.altair288.class_management.model.Class clazz8 = mkClass("22物联网技术2班", teacher8, "2022");

            Department deptInfo = mkDepartment("信息系", "INFO", "信息工程与计算机相关专业");
            Department deptMech = mkDepartment("机电系", "MECH", "机械与电气相关专业");
            Department deptFash = mkDepartment("服装系", "FASH", "服装与设计相关专业");

            bindClassDepartment(clazz1, deptInfo);
            bindClassDepartment(clazz2, deptInfo);
            bindClassDepartment(clazz3, deptMech);
            bindClassDepartment(clazz4, deptMech);
            bindClassDepartment(clazz5, deptInfo);
            bindClassDepartment(clazz6, deptInfo);
            bindClassDepartment(clazz7, deptFash);
            bindClassDepartment(clazz8, deptFash);

            Student student1 = mkStudent("王学生", "s001", clazz1);
            Student student = mkStudent("李学生", "S2024001", clazz1);

            Parent parent = new Parent();
            parent.setName("王家长");
            parent.setPhone("13700000001");
            parent.setEmail("parent@example.com");
            parent.setStudent(student);
            parent = parentService.save(parent);

            Teacher[] teacherArr = { teacher, teacher2, teacher3, teacher4, teacher5, teacher6, teacher7, teacher8,
                    teacher9, teacher10, teacher11, teacher12, teacher13, teacher14, teacher15, teacher16, teacher17 };
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

            registerStudentUser(student1, initialPassword);
            User studentUser = registerStudentUser(student, initialPassword);

            User parentUser = new User(null);
            parentUser.setUsername(parent.getName());
            parentUser.setIdentityNo(parent.getPhone());
            parentUser.setPassword(initialPassword);
            parentUser.setUserType(User.UserType.PARENT);
            parentUser.setRelatedId(parent.getId());
            parentUser = userService.registerUser(parentUser);

            User adminUser = new User(null);
            adminUser.setUsername("admin");
            adminUser.setIdentityNo("admin");
            adminUser.setPassword(initialPassword);
            adminUser.setUserType(User.UserType.ADMIN);
            adminUser = userService.registerUser(adminUser);

            userRoleService.assignRoleToUser(adminUser.getId(), adminRole.getId());
            for (User tu : teacherUserMap.values())
                userRoleService.assignRoleToUser(tu.getId(), teacherRole.getId());
            userRoleService.assignRoleToUser(studentUser.getId(), studentRole.getId());
            userRoleService.assignRoleToUser(parentUser.getId(), parentRole.getId());

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

            com.altair288.class_management.model.Class cls2024a = mkClass("计算机2024-1班", teacher9, "2024");
            com.altair288.class_management.model.Class cls2024b = mkClass("计算机2024-2班", teacher10, "2024");
            bindClassDepartment(cls2024a, deptInfo);
            bindClassDepartment(cls2024b, deptMech);

            Role homeroomApprovalRole = roleService.getByCode(Role.Codes.HOMEROOM);
            java.util.function.BiConsumer<com.altair288.class_management.model.Class, Teacher> assignHomeroom = (c,
                    t) -> {
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

            Student sZhang3 = mkStudent("张三", "2024001", cls2024a);
            Student sLi4 = mkStudent("李四", "2024002", cls2024a);
            Student sWang5 = mkStudent("王五", "2024003", cls2024b);
            Student sZhao6 = mkStudent("赵六", "2024004", cls2024b);

            // 为张三/李四/王五/赵六创建用户（若不存在）并绑定学生角色
            java.util.function.Consumer<Student> ensureStudentUser = stu -> {
                try {
                    var existing = userRepository.findByUsernameOrIdentityNo(stu.getStudentNo());
                    User u;
                    if (existing.isPresent()) {
                        u = existing.get();
                    } else {
                        u = new User(null);
                        u.setUsername(stu.getName());
                        u.setIdentityNo(stu.getStudentNo());
                        u.setPassword(initialPassword);
                        u.setUserType(User.UserType.STUDENT);
                        u.setRelatedId(stu.getId());
                        u = userService.registerUser(u);
                    }
                    // 赋予学生角色（幂等：底层若无重复约束需自行判重，这里简单尝试）
                    try { userRoleService.assignRoleToUser(u.getId(), studentRole.getId()); } catch (Exception ignore) {}
                } catch (Exception ex) {
                    logger.warn("为学生 {} 创建或绑定用户失败: {}", stu.getStudentNo(), ex.getMessage());
                }
            };
            ensureStudentUser.accept(sZhang3);
            ensureStudentUser.accept(sLi4);
            ensureStudentUser.accept(sWang5);
            ensureStudentUser.accept(sZhao6);

            createCreditItemsIfAbsent();
            Integer deId = creditItemRepository.findAllByCategory("德").get(0).getId();
            Integer zhiId = creditItemRepository.findAllByCategory("智").get(0).getId();
            Integer tiId = creditItemRepository.findAllByCategory("体").get(0).getId();
            Integer meiId = creditItemRepository.findAllByCategory("美").get(0).getId();
            Integer laoId = creditItemRepository.findAllByCategory("劳").get(0).getId();

            studentCreditService.setScore(sZhang3.getId(), deId, 85.0, "初始化数据");
            studentCreditService.setScore(sZhang3.getId(), zhiId, 92.0, "初始化数据");
            studentCreditService.setScore(sZhang3.getId(), tiId, 78.0, "初始化数据");
            studentCreditService.setScore(sZhang3.getId(), meiId, 88.0, "初始化数据");
            studentCreditService.setScore(sZhang3.getId(), laoId, 85.0, "初始化数据");

            studentCreditService.setScore(sLi4.getId(), deId, 75.0, "初始化数据");
            studentCreditService.setScore(sLi4.getId(), zhiId, 88.0, "初始化数据");
            studentCreditService.setScore(sLi4.getId(), tiId, 82.0, "初始化数据");
            studentCreditService.setScore(sLi4.getId(), meiId, 76.0, "初始化数据");
            studentCreditService.setScore(sLi4.getId(), laoId, 80.0, "初始化数据");

            studentCreditService.setScore(sWang5.getId(), deId, 60.0, "初始化数据");
            studentCreditService.setScore(sWang5.getId(), zhiId, 70.0, "初始化数据");
            studentCreditService.setScore(sWang5.getId(), tiId, 65.0, "初始化数据");
            studentCreditService.setScore(sWang5.getId(), meiId, 58.0, "初始化数据");
            studentCreditService.setScore(sWang5.getId(), laoId, 62.0, "初始化数据");

            studentCreditService.setScore(sZhao6.getId(), deId, 45.0, "初始化数据");
            studentCreditService.setScore(sZhao6.getId(), zhiId, 55.0, "初始化数据");
            studentCreditService.setScore(sZhao6.getId(), tiId, 50.0, "初始化数据");
            studentCreditService.setScore(sZhao6.getId(), meiId, 48.0, "初始化数据");
            studentCreditService.setScore(sZhao6.getId(), laoId, 52.0, "初始化数据");

            LeaveTypeConfig sickLeaveConfig = ensureLeaveType("sick", "病假", 90, 10, true, true, "#388e3c",
                    "因病需要休息，需提供医疗证明");
            LeaveTypeConfig personalLeaveConfig = ensureLeaveType("personal", "事假", 10, 5, true, false, "#f57c00",
                    "因个人事务需要请假");
            LeaveTypeConfig annualLeaveConfig = ensureLeaveType("annual", "年假", 30, 15, true, false, "#1976d2",
                    "每年享有的带薪年假，需提前申请");

            Integer currentYear = 2025;
            initBalances(currentYear, sZhang3, sickLeaveConfig, personalLeaveConfig, annualLeaveConfig);
            initBalances(currentYear, sLi4, sickLeaveConfig, personalLeaveConfig, annualLeaveConfig);
            initBalances(currentYear, sWang5, sickLeaveConfig, personalLeaveConfig, annualLeaveConfig);
            initBalances(currentYear, sZhao6, sickLeaveConfig, personalLeaveConfig, annualLeaveConfig);
            studentLeaveBalanceService.initializeBalancesForStudentAllEnabled(student1.getId(), currentYear);
            studentLeaveBalanceService.initializeBalancesForStudentAllEnabled(student.getId(), currentYear);

            logger.info("测试数据初始化完成: admin/教师/学生/家长示例账号、学分、请假类型与余额已生成");
        } catch (Exception e) {
            logger.error("初始化测试数据时发生错误", e);
        }
    }

    private Teacher mkTeacher(String name, String no, String phone, String email) {
        Teacher t = new Teacher();
        t.setName(name);
        t.setTeacherNo(no);
        t.setPhone(phone);
        t.setEmail(email);
        return teacherService.save(t);
    }

    private com.altair288.class_management.model.Class mkClass(String name, Teacher teacher, String grade) {
        com.altair288.class_management.model.Class c = new com.altair288.class_management.model.Class();
        c.setName(name);
        c.setTeacher(teacher);
        c.setGrade(grade);
        return classService.save(c);
    }

    private Department mkDepartment(String name, String code, String desc) {
        return departmentRepository.findByCode(code).orElseGet(() -> {
            Department d = new Department();
            d.setName(name);
            d.setCode(code);
            d.setDescription(desc);
            d.setEnabled(true);
            return departmentRepository.save(d);
        });
    }

    private void bindClassDepartment(com.altair288.class_management.model.Class c, Department dept) {
        c.setDepartment(dept);
        classService.save(c);
    }

    private Student mkStudent(String name, String no, com.altair288.class_management.model.Class clazz) {
        Student s = new Student();
        s.setName(name);
        s.setStudentNo(no);
        s.setClazz(clazz);
        return studentService.save(s);
    }

    private User registerStudentUser(Student s, String pwd) {
        User u = new User(null);
        u.setUsername(s.getName());
        u.setIdentityNo(s.getStudentNo());
        u.setPassword(pwd);
        u.setUserType(User.UserType.STUDENT);
        u.setRelatedId(s.getId());
        return userService.registerUser(u);
    }

    private void createCreditItemsIfAbsent() {
        if (!creditItemRepository.existsByCategory("德"))
            creditItemService.create(new CreditItemDTO(null, "德", "德育", 100.0, 100.0, true, "思想品德与道德修养"));
        if (!creditItemRepository.existsByCategory("智"))
            creditItemService.create(new CreditItemDTO(null, "智", "智育", 100.0, 100.0, true, "学业成绩与知识掌握"));
        if (!creditItemRepository.existsByCategory("体"))
            creditItemService.create(new CreditItemDTO(null, "体", "体育", 100.0, 100.0, true, "身体素质与健康状况"));
        if (!creditItemRepository.existsByCategory("美"))
            creditItemService.create(new CreditItemDTO(null, "美", "美育", 100.0, 100.0, true, "艺术修养与审美能力"));
        if (!creditItemRepository.existsByCategory("劳"))
            creditItemService.create(new CreditItemDTO(null, "劳", "劳育", 100.0, 100.0, true, "劳动技能与实践能力"));
    }

    private LeaveTypeConfig ensureLeaveType(String code, String name, int max, int allowance, boolean approval,
            boolean medical, String color, String desc) {
        LeaveTypeConfig c = leaveTypeConfigRepository.findByTypeCode(code);
        if (c != null)
            return c;
        LeaveTypeConfig n = new LeaveTypeConfig();
        n.setTypeCode(code);
        n.setTypeName(name);
        n.setMaxDaysPerRequest(max);
        n.setAnnualAllowance(allowance);
        n.setRequiresApproval(approval);
        n.setRequiresMedicalProof(medical);
        n.setAdvanceDaysRequired(1);
        n.setEnabled(true);
        n.setColor(color);
        n.setDescription(desc);
        return leaveTypeConfigService.saveLeaveType(n);
    }

    private void initBalances(Integer year, Student stu, LeaveTypeConfig... types) {
        for (LeaveTypeConfig t : types) {
            studentLeaveBalanceService.initializeStudentBalance(stu.getId(), t.getId(), year, t.getAnnualAllowance());
        }
    }
}
