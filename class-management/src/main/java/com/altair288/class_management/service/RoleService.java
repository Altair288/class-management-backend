package com.altair288.class_management.service;

import com.altair288.class_management.model.Role;
import com.altair288.class_management.repository.RoleRepository;
import org.springframework.data.domain.Sort;
import com.altair288.class_management.repository.UserRoleRepository;
import com.altair288.class_management.repository.RoleAssignmentRepository;
import com.altair288.class_management.repository.ApprovalStepRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final ApprovalStepRepository approvalStepRepository;

    public RoleService(RoleRepository roleRepository,
                       UserRoleRepository userRoleRepository,
                       RoleAssignmentRepository roleAssignmentRepository,
                       ApprovalStepRepository approvalStepRepository) {
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.approvalStepRepository = approvalStepRepository;
    }

    public Role createRole(Role role) {
        if (role.getCode() == null || role.getCode().isBlank()) {
            throw new IllegalArgumentException("角色 code 不能为空");
        }
        return roleRepository.save(role);
    }

    public Role getByCode(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("角色不存在: " + code));
    }

    public java.util.List<Role> listApprovalRoles() {
        return roleRepository.findByCategoryOrderByLevelAscSortOrderAsc(Role.Category.APPROVAL);
    }

    public java.util.List<Role> listSystemRoles() {
        return roleRepository.findByCategoryOrderByLevelAscSortOrderAsc(Role.Category.SYSTEM);
    }

    public java.util.List<Role> listAll() {
        return roleRepository.findAll(Sort.by(Sort.Direction.ASC, "category", "level", "sortOrder"));
    }

    public Role update(Integer id, java.util.function.Consumer<Role> updater) {
        Role r = roleRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("角色不存在: " + id));
        updater.accept(r);
        return roleRepository.save(r);
    }

    public void delete(Integer id) {
        Role r = roleRepository.findById(id).orElse(null);
        if (r == null) return;
        // 引用检查：用户角色、审批步骤、角色指派
        boolean inUserRole = !userRoleRepository.findByRoleId(id).isEmpty();
        boolean inAssignments = roleAssignmentRepository.findAll().stream().anyMatch(a -> a.getApprovalRole()!=null && id.equals(a.getApprovalRole().getId()));
        boolean inApprovalStep = approvalStepRepository.findAll().stream().anyMatch(s -> s.getApproverRole()!=null && id.equals(s.getApproverRole().getId()));
        if (inUserRole || inAssignments || inApprovalStep) {
            StringBuilder sb = new StringBuilder("角色仍被引用：");
            if (inUserRole) sb.append("[用户绑定] ");
            if (inAssignments) sb.append("[审批人指派] ");
            if (inApprovalStep) sb.append("[审批流程步骤] ");
            throw new IllegalStateException(sb.toString().trim());
        }
        roleRepository.deleteById(id);
    }

    public Role updateHierarchy(Integer id, Integer level, Integer sortOrder) {
        return update(id, r -> {
            if (level != null) r.setLevel(level);
            if (sortOrder != null) r.setSortOrder(sortOrder);
        });
    }

    public Role toggleEnable(Integer id, Boolean enabled) {
        return update(id, r -> { if (enabled != null) r.setEnabled(enabled); });
    }

    // =============== Usage 统计 ===============
    @Transactional(readOnly = true)
    public Map<Integer, UsageStat> computeUsage(List<Integer> roleIds) {
        // 为避免 N+1，使用简单分组计数（此处仍然是多查询；可进一步用自定义 count 查询优化）
    Map<Integer, Long> userCounts = userRoleRepository.findAll().stream()
        .filter(ur -> ur.getRole()!=null && ur.getRole().getId()!=null && roleIds.contains(ur.getRole().getId()))
        .collect(Collectors.groupingBy(ur -> ur.getRole().getId(), Collectors.counting()));
        Map<Integer, Long> assignmentCounts = roleAssignmentRepository.findAll().stream()
                .filter(a -> a.getApprovalRole()!=null && roleIds.contains(a.getApprovalRole().getId()))
                .collect(Collectors.groupingBy(a -> a.getApprovalRole().getId(), Collectors.counting()));
        Map<Integer, Long> stepCounts = approvalStepRepository.findAll().stream()
                .filter(s -> s.getApproverRole()!=null && roleIds.contains(s.getApproverRole().getId()))
                .collect(Collectors.groupingBy(s -> s.getApproverRole().getId(), Collectors.counting()));
        return roleIds.stream().collect(Collectors.toMap(id -> id, id -> new UsageStat(
                userCounts.getOrDefault(id, 0L),
                stepCounts.getOrDefault(id, 0L),
                assignmentCounts.getOrDefault(id, 0L)
        )));
    }

    public static class UsageStat {
        public final long userCount;
        public final long approvalStepCount;
        public final long assignmentCount;
        public UsageStat(long userCount, long approvalStepCount, long assignmentCount) {
            this.userCount = userCount; this.approvalStepCount = approvalStepCount; this.assignmentCount = assignmentCount; }
    }

    // 批量层级/排序更新
    @Transactional
    public void batchUpdateHierarchy(List<HierarchyPatch> patches) {
        Map<Integer, HierarchyPatch> byId = patches.stream().collect(Collectors.toMap(p -> p.id, p -> p));
        List<Role> roles = roleRepository.findAllById(byId.keySet());
        for (Role r: roles) {
            HierarchyPatch p = byId.get(r.getId());
            if (p.level != null) r.setLevel(p.level);
            if (p.sortOrder != null) r.setSortOrder(p.sortOrder);
        }
        roleRepository.saveAll(roles);
    }

    public static class HierarchyPatch {
        public Integer id; public Integer level; public Integer sortOrder;
        public HierarchyPatch() {}
        public HierarchyPatch(Integer id, Integer level, Integer sortOrder) { this.id=id; this.level=level; this.sortOrder=sortOrder; }
    }
}