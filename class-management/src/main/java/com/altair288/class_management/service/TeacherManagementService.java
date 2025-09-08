package com.altair288.class_management.service;

import com.altair288.class_management.dto.RoleScopeDTO;
import com.altair288.class_management.dto.TeacherManagementDTO;
import com.altair288.class_management.dto.UpdateTeacherRolesRequest;
import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Department;
import com.altair288.class_management.model.RoleAssignment;
import com.altair288.class_management.model.Teacher;
import com.altair288.class_management.repository.ClassRepository;
import com.altair288.class_management.repository.RoleAssignmentRepository;
import com.altair288.class_management.repository.TeacherRepository;
import com.altair288.class_management.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeacherManagementService {
    private final TeacherRepository teacherRepository;
    private final ClassRepository classRepository;
    private final RoleAssignmentRepository roleAssignmentRepository;
    private final DepartmentRepository departmentRepository;

    public TeacherManagementService(TeacherRepository teacherRepository,
                                    ClassRepository classRepository,
                                    RoleAssignmentRepository roleAssignmentRepository,
                                    DepartmentRepository departmentRepository) {
        this.teacherRepository = teacherRepository;
        this.classRepository = classRepository;
        this.roleAssignmentRepository = roleAssignmentRepository;
        this.departmentRepository = departmentRepository;
    }

    public List<TeacherManagementDTO> listAll() {
        List<Teacher> teachers = teacherRepository.findAll();
        return buildDTOs(teachers);
    }

    public TeacherManagementDTO getOne(Integer id) {
        Teacher t = teacherRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("教师不存在"));
        return buildDTOs(List.of(t)).get(0);
    }

    // 批量构建，避免 N+1
    private List<TeacherManagementDTO> buildDTOs(List<Teacher> teachers) {
        if (teachers.isEmpty()) return List.of();
        List<Integer> teacherIds = teachers.stream().map(Teacher::getId).toList();

        // 1) 班主任班级（含系部）一次性加载
        List<Class> homeroomClasses = classRepository.findWithDepartmentByTeacherIds(teacherIds);
        Map<Integer, Class> teacherHomeroomClass = new HashMap<>();
        for (Class c : homeroomClasses) {
            teacherHomeroomClass.putIfAbsent(c.getTeacher().getId(), c); // 取第一条
        }

        // 2) 角色批量
        List<RoleAssignment> assignments = roleAssignmentRepository.findByTeacherIdIn(teacherIds);

        // 收集 classIds 与 departmentIds 供角色作用域展示
        Set<Integer> roleClassIds = assignments.stream().filter(a->a.getClassId()!=null).map(RoleAssignment::getClassId).collect(Collectors.toSet());
        // 已包含的班主任班级ID不重复加载
        roleClassIds.removeAll(homeroomClasses.stream().map(Class::getId).collect(Collectors.toSet()));
        List<Class> roleScopeExtraClasses = roleClassIds.isEmpty()? List.of() : classRepository.findWithDepartmentByIds(roleClassIds.stream().toList());

        Map<Integer, Class> classById = new HashMap<>();
        for (Class c : homeroomClasses) classById.put(c.getId(), c);
        for (Class c : roleScopeExtraClasses) classById.put(c.getId(), c);

        // departmentIds from: homeroom classes + role assignments' departmentId + classes fetched
        Set<Integer> departmentIds = new HashSet<>();
        homeroomClasses.stream().filter(c->c.getDepartment()!=null).forEach(c->departmentIds.add(c.getDepartment().getId()));
        roleScopeExtraClasses.stream().filter(c->c.getDepartment()!=null).forEach(c->departmentIds.add(c.getDepartment().getId()));
        assignments.stream().filter(a->a.getDepartmentId()!=null).forEach(a->departmentIds.add(a.getDepartmentId()));
        Map<Integer, Department> deptMap = departmentIds.isEmpty()? Map.of() : departmentRepository.findAllById(departmentIds).stream().collect(Collectors.toMap(Department::getId, d->d));

        // 3) 分组角色
        Map<Integer, List<RoleAssignment>> rolesByTeacher = assignments.stream().collect(Collectors.groupingBy(RoleAssignment::getTeacherId));

        // 4) 构建 DTO
        List<TeacherManagementDTO> result = new ArrayList<>(teachers.size());
        for (Teacher t : teachers) {
            TeacherManagementDTO dto = new TeacherManagementDTO(t.getId(), t.getName(), t.getTeacherNo(), t.getPhone(), t.getEmail());
            Class hc = teacherHomeroomClass.get(t.getId());
            if (hc != null) {
                dto.setHomeroomClassId(hc.getId());
                dto.setHomeroomClassName(hc.getName());
                dto.setGrade(hc.getGrade());
                if (hc.getDepartment()!=null) {
                    dto.setDepartmentId(hc.getDepartment().getId());
                    dto.setDepartmentName(hc.getDepartment().getName());
                    dto.setDepartmentCode(hc.getDepartment().getCode());
                }
            }
            List<RoleScopeDTO> roleDtos = rolesByTeacher.getOrDefault(t.getId(), List.of()).stream().map(r -> {
                String scopeType;
                if (r.getClassId() != null) scopeType = "class";
                else if (r.getDepartmentId() != null) scopeType = "department";
                else if (r.getGrade() != null) scopeType = "grade";
                else scopeType = "global";
                String className = null;
                if (r.getClassId() != null) {
                    Class rc = classById.get(r.getClassId());
                    className = rc != null ? rc.getName() : null;
                }
                String departmentName = null;
                if (r.getDepartmentId() != null) {
                    Department d = deptMap.get(r.getDepartmentId());
                    departmentName = d != null ? d.getName() : null;
                }
                return new RoleScopeDTO(r.getId(), r.getRole(), r.getClassId(), className, r.getDepartmentId(), departmentName, r.getGrade(), scopeType);
            }).collect(Collectors.toList());
            dto.setRoles(roleDtos);

            // 若没有班主任班级提供的顶层 department / grade，则尝试从角色指派推导
            if (dto.getDepartmentId() == null) {
                roleDtos.stream()
                        .filter(r -> r.getDepartmentId() != null)
                        .findFirst()
                        .ifPresent(r -> {
                            dto.setDepartmentId(r.getDepartmentId());
                            dto.setDepartmentName(r.getDepartmentName());
                            // 尝试补 departmentCode：从 deptMap 中取
                            Department d = deptMap.get(r.getDepartmentId());
                            if (d != null) dto.setDepartmentCode(d.getCode());
                        });
            }
            if (dto.getGrade() == null) {
                roleDtos.stream()
                        .filter(r -> r.getGrade() != null)
                        .findFirst()
                        .ifPresent(r -> dto.setGrade(r.getGrade()));
            }
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public TeacherManagementDTO update(Integer teacherId, UpdateTeacherRolesRequest req) {
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new IllegalArgumentException("教师不存在"));

        // 处理班主任班级调整
        List<Class> current = classRepository.findByTeacher_Id(teacherId);
        Integer newClassId = req.getHomeroomClassId();
        if (newClassId != null) {
            Class newClass = classRepository.findById(newClassId).orElseThrow(() -> new IllegalArgumentException("班级不存在"));
            // 如果该班级已有其他班主任，覆盖
            if (newClass.getTeacher() == null || !Objects.equals(newClass.getTeacher().getId(), teacherId)) {
                newClass.setTeacher(teacher);
                classRepository.save(newClass);
            }
            // 取消其他班级的班主任身份（保证唯一）
            for (Class c : current) {
                if (!Objects.equals(c.getId(), newClassId)) {
                    c.setTeacher(null);
                    classRepository.save(c);
                }
            }
        } else {
            // 取消所有班主任班级
            for (Class c : current) {
                c.setTeacher(null);
                classRepository.save(c);
            }
        }

        // 处理角色：完整覆盖策略（根据输入列表重建）
        // 支持：纯行政教师（无班主任班级）也可直接分配年级主任/系部主任/全局角色
        // 若有重复键，后者覆盖前者
        Map<String, UpdateTeacherRolesRequest.RoleAssignmentInput> desired = new LinkedHashMap<>();
        for (UpdateTeacherRolesRequest.RoleAssignmentInput r : Optional.ofNullable(req.getRoles()).orElse(List.of())) {
            String key = r.getRole() + "|" + (r.getClassId()==null?"":r.getClassId()) + "|" + (r.getDepartmentId()==null?"":r.getDepartmentId()) + "|" + (r.getGrade()==null?"":r.getGrade());
            desired.put(key, r);
        }
        List<RoleAssignment> existing = roleAssignmentRepository.findByTeacherId(teacherId);
        // 删除不在 desired 的
        for (RoleAssignment ra : existing) {
            String key = ra.getRole() + "|" + n(ra.getClassId()) + "|" + n(ra.getDepartmentId()) + "|" + n(ra.getGrade());
            if (!desired.containsKey(key)) {
                roleAssignmentRepository.deleteById(ra.getId());
            }
        }
        // 新建或更新
        for (UpdateTeacherRolesRequest.RoleAssignmentInput in : desired.values()) {
            // 校验系部存在
            if (in.getDepartmentId() != null && !departmentRepository.findById(in.getDepartmentId()).isPresent()) {
                throw new IllegalArgumentException("系部不存在: " + in.getDepartmentId());
            }
            RoleAssignment match = existing.stream().filter(ra -> ra.getRole().equals(in.getRole()) && Objects.equals(ra.getClassId(), in.getClassId()) && Objects.equals(ra.getDepartmentId(), in.getDepartmentId()) && Objects.equals(ra.getGrade(), in.getGrade())).findFirst().orElse(null);
            if (match == null) {
                RoleAssignment na = new RoleAssignment();
                na.setRole(in.getRole());
                na.setTeacherId(teacherId);
                na.setClassId(in.getClassId());
                na.setDepartmentId(in.getDepartmentId());
                na.setGrade(in.getGrade());
                na.setEnabled(in.getEnabled());
                na.setCreatedAt(new Date());
                na.setUpdatedAt(new Date());
                roleAssignmentRepository.save(na);
            } else {
                match.setEnabled(in.getEnabled());
                match.setUpdatedAt(new Date());
                roleAssignmentRepository.save(match);
            }
        }
    return getOne(teacherId);
    }

    private String n(Object o) { return o == null ? "" : String.valueOf(o); }
}
