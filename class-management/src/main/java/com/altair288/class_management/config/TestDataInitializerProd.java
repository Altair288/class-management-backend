package com.altair288.class_management.config;

import com.altair288.class_management.model.Role;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.Department;
import com.altair288.class_management.dto.CreditItemDTO;
import com.altair288.class_management.repository.UserRepository;
import com.altair288.class_management.repository.DepartmentRepository;
import com.altair288.class_management.repository.CreditItemRepository;
import com.altair288.class_management.service.RoleService;
import com.altair288.class_management.service.UserRoleService;
import com.altair288.class_management.service.UserService;
import com.altair288.class_management.service.CreditItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * 生产环境管理员/基础数据引导初始化：
 *  1. 使用环境变量 APP_BOOTSTRAP_ADMIN_USERNAME / APP_BOOTSTRAP_ADMIN_PASSWORD 创建或维护管理员账号
 *  2. 幂等创建基础“系部”数据 (INFO / MECH / FASH)
 *  3. 幂等创建初始 5 个学分分类 (德/智/体/美/劳)
 *  4. 可通过 APP_BOOTSTRAP_ADMIN_RESET_IF_EXISTS=true 强制重置管理员密码
 *
 * 说明：所有操作幂等，可安全重复启动。
 */
@Component
@Profile("prod")
public class TestDataInitializerProd {
    private static final Logger log = LoggerFactory.getLogger(TestDataInitializerProd.class);

    @Value("${APP_BOOTSTRAP_ADMIN_USERNAME:}")
    private String adminUsername;
    @Value("${APP_BOOTSTRAP_ADMIN_PASSWORD:}")
    private String adminPassword;
    @Value("${APP_BOOTSTRAP_ADMIN_RESET_IF_EXISTS:false}")
    private boolean resetIfExists;

    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final DepartmentRepository departmentRepository;
    private final CreditItemRepository creditItemRepository;
    private final CreditItemService creditItemService;

    public TestDataInitializerProd(UserRepository userRepository,
                                   UserService userService,
                                   RoleService roleService,
                                   UserRoleService userRoleService,
                                   DepartmentRepository departmentRepository,
                                   CreditItemRepository creditItemRepository,
                                   CreditItemService creditItemService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
        this.departmentRepository = departmentRepository;
        this.creditItemRepository = creditItemRepository;
        this.creditItemService = creditItemService;
    }

    @PostConstruct
    public void init() {
        try {
            ensureDepartments();
            ensureCreditItems();
            bootstrapAdmin();
        } catch (Exception e) {
            log.error("[prod-bootstrap] 初始化过程出现未捕获异常: {}", e.getMessage(), e);
        }
    }

    /* ================= 管理员 ================= */
    private void bootstrapAdmin() {
        if (isBlank(adminUsername) || isBlank(adminPassword)) {
            log.info("[prod-bootstrap] 未配置管理员用户名或密码，跳过管理员创建 (可设置 APP_BOOTSTRAP_ADMIN_USERNAME / PASSWORD)");
            return;
        }
        try {
            Role adminRole = roleService.getByCode(Role.Codes.ADMIN);
            User existing = userRepository.findByUsernameOrIdentityNo(adminUsername).orElse(null);
            if (existing == null) {
                User admin = new User(null);
                admin.setUsername(adminUsername);
                admin.setIdentityNo(adminUsername);
                admin.setPassword(adminPassword);
                admin.setUserType(User.UserType.ADMIN);
                admin = userService.registerUser(admin);
                userRoleService.assignRoleToUser(admin.getId(), adminRole.getId());
                log.info("[prod-bootstrap] 已创建管理员用户 '{}' 并分配 ADMIN 角色", adminUsername);
            } else {
                if (resetIfExists) {
                    existing.setPassword(userService.getPasswordEncoder().encode(adminPassword));
                    userRepository.save(existing);
                    log.info("[prod-bootstrap] 已重置管理员 '{}' 密码", adminUsername);
                }
                boolean hasAdmin = existing.getUserRoles().stream()
                        .anyMatch(ur -> ur.getRole()!=null && Role.Codes.ADMIN.equals(ur.getRole().getCode()));
                if (!hasAdmin) {
                    userRoleService.assignRoleToUser(existing.getId(), adminRole.getId());
                    log.info("[prod-bootstrap] 为已有管理员 '{}' 补齐 ADMIN 角色绑定", adminUsername);
                }
            }
        } catch (Exception e) {
            log.error("[prod-bootstrap] 管理员引导失败: {}", e.getMessage(), e);
        }
    }

    /* ================= 系部数据 ================= */
    private void ensureDepartments() {
        ensureDepartment("INFO", "信息系", "信息工程与计算机相关专业");
        ensureDepartment("MECH", "机电系", "机械与电气相关专业");
        ensureDepartment("FASH", "服装系", "服装与设计相关专业");
    }

    private void ensureDepartment(String code, String name, String desc) {
        departmentRepository.findByCode(code).orElseGet(() -> {
            Department d = new Department();
            d.setCode(code);
            d.setName(name);
            d.setDescription(desc);
            d.setEnabled(true);
            Department saved = departmentRepository.save(d);
            log.info("[prod-bootstrap] 创建系部 {}({})", name, code);
            return saved;
        });
    }

    /* ================= 学分分类 ================= */
    private void ensureCreditItems() {
        // 与 dev 中 createCreditItemsIfAbsent 同步：满分 100，初始值 100（可改为 0 依据实际业务）
        createCreditIfAbsent("德", "德育", "思想品德与道德修养");
        createCreditIfAbsent("智", "智育", "学业成绩与知识掌握");
        createCreditIfAbsent("体", "体育", "身体素质与健康状况");
        createCreditIfAbsent("美", "美育", "艺术修养与审美能力");
        createCreditIfAbsent("劳", "劳育", "劳动技能与实践能力");
    }

    private void createCreditIfAbsent(String category, String itemName, String desc) {
        if (!creditItemRepository.existsByCategory(category)) {
            creditItemService.create(new CreditItemDTO(null, category, itemName, 100.0, 100.0, true, desc));
            log.info("[prod-bootstrap] 创建学分类别 {} -> {}", category, itemName);
        }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
