package com.altair288.class_management.controller;

import com.altair288.class_management.dto.ClassInfoDTO;
import com.altair288.class_management.dto.ClassListDTO;
import com.altair288.class_management.dto.ClassSimpleDTO;
import com.altair288.class_management.dto.ClassStudentCountDTO;
import com.altair288.class_management.dto.CreateClassDTO;
import com.altair288.class_management.dto.StudentDTO;
import com.altair288.class_management.dto.AddStudentDTO;
import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.Department;
import com.altair288.class_management.model.Teacher;
import com.altair288.class_management.service.ClassService;
import com.altair288.class_management.service.StudentService;
import com.altair288.class_management.service.TeacherService;
import com.altair288.class_management.repository.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import com.altair288.class_management.dto.ImportStudentsResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/api/class")
public class ClassController {

    private final ClassService classService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final DepartmentRepository departmentRepository;

    public ClassController(ClassService classService, StudentService studentService, TeacherService teacherService, DepartmentRepository departmentRepository) {
        this.classService = classService;
        this.studentService = studentService;
        this.teacherService = teacherService;
        this.departmentRepository = departmentRepository;
    }

    // 查询所有班级详细信息
    @GetMapping("/all")
    public ResponseEntity<List<ClassInfoDTO>> getAllClasses() {
        List<Class> classes = classService.findAll();
        List<ClassInfoDTO> result = classes.stream().map(c -> {
        String teacherName = c.getTeacher() != null ? c.getTeacher().getName() : null;
        Integer teacherId = c.getTeacher() != null ? c.getTeacher().getId() : null;
            // createdAt 类型转换
            Timestamp createdAt = c.getCreatedAt() == null ? null : new Timestamp(c.getCreatedAt().getTime());
            return new ClassInfoDTO(
                    c.getId(),
                    c.getName(),
                    c.getGrade(), // 新增
            teacherName,
            teacherId,
                    createdAt);
        }).toList();
        return ResponseEntity.ok(result);
    }

    // 查询所有班级简要信息
    @GetMapping("/simple")
    public ResponseEntity<List<ClassSimpleDTO>> getSimpleClasses() {
        List<Class> classes = classService.findAll();
        List<ClassSimpleDTO> result = classes.stream()
                .map(c -> new ClassSimpleDTO(c.getId(), c.getName(), c.getGrade()))
                .toList();
        return ResponseEntity.ok(result);
    }

    // 前端列表：包含系部与班主任教师（避免前端再做额外查询）
    @GetMapping("/list")
    public ResponseEntity<List<ClassListDTO>> getClassList() {
        List<Class> classes = classService.findAll();
        List<ClassListDTO> result = classes.stream().map(c -> new ClassListDTO(
                c.getId(),
                c.getName(),
                c.getGrade(),
                c.getDepartment()!=null?c.getDepartment().getId():null,
                c.getDepartment()!=null?c.getDepartment().getName():null,
                c.getTeacher()!=null?c.getTeacher().getId():null,
                c.getTeacher()!=null?c.getTeacher().getName():null
        )).toList();
        return ResponseEntity.ok(result);
    }

    // 查询班级数量
    @GetMapping("/count")
    public ResponseEntity<Long> getClassCount() {
        long count = classService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/student-count")
    public ResponseEntity<List<ClassStudentCountDTO>> getClassStudentCounts() {
        List<Class> classes = classService.findAll();
        List<ClassStudentCountDTO> result = classes.stream().map(c -> {
            Long count = studentService.countByClassId(c.getId());
            return new ClassStudentCountDTO(c.getId(), c.getName(), count);
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createClass(@RequestBody CreateClassDTO dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            return ResponseEntity.badRequest().body("班级名称不能为空");
        }
        if (dto.getGrade() == null || dto.getGrade().isBlank()) {
            return ResponseEntity.badRequest().body("年级不能为空");
        }
        if (dto.getDepartmentId() == null) {
            return ResponseEntity.badRequest().body("系部不能为空");
        }
        // 验证系部是否存在
        Department dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
        if (dept == null) return ResponseEntity.badRequest().body("系部不存在: " + dto.getDepartmentId());
    Class clazz = new Class();
        clazz.setName(dto.getName());
        clazz.setGrade(dto.getGrade());
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherService.getById(dto.getTeacherId());
            if (teacher == null) return ResponseEntity.badRequest().body("教师不存在: " + dto.getTeacherId());
            clazz.setTeacher(teacher);
        }
        clazz.setDepartment(dept);
        classService.save(clazz);
        return ResponseEntity.ok().build();
    }

    // 更新班级（名称/年级/系部/班主任）
    @PutMapping("/{classId}")
    public ResponseEntity<?> updateClass(@PathVariable Integer classId, @RequestBody CreateClassDTO dto) {
        Class clazz = classService.getById(classId);
        if (clazz == null) return ResponseEntity.badRequest().body("班级不存在: " + classId);
        if (dto.getName() != null && !dto.getName().isBlank()) clazz.setName(dto.getName());
        if (dto.getGrade() != null && !dto.getGrade().isBlank()) clazz.setGrade(dto.getGrade());
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherService.getById(dto.getTeacherId());
            if (teacher == null) return ResponseEntity.badRequest().body("教师不存在: " + dto.getTeacherId());
            clazz.setTeacher(teacher);
        }
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId()).orElse(null);
            if (dept == null) return ResponseEntity.badRequest().body("系部不存在: " + dto.getDepartmentId());
            clazz.setDepartment(dept);
        }
        classService.save(clazz);
        return ResponseEntity.ok().build();
    }

    // 查询班级成员
    @GetMapping("/{classId}/members")
    public ResponseEntity<List<StudentDTO>> getClassMembers(@PathVariable Integer classId) {
        List<Student> students = studentService.findByClassId(classId);
        List<StudentDTO> result = students.stream()
                .map(s -> new StudentDTO(
                        s.getId(),
                        s.getName(),
                        s.getStudentNo(),
                        s.getPhone(),          // 新增
                        s.getEmail()           // 新增
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{classId}/add-student")
    public ResponseEntity<?> addStudentToClass(@PathVariable Integer classId, @RequestBody AddStudentDTO dto) {
        Student student = studentService.getStudentById(dto.getStudentId());
        Class clazz = classService.getById(classId);
        if (student == null || clazz == null) {
            return ResponseEntity.badRequest().body("学生或班级不存在");
        }
        student.setClazz(clazz);
        studentService.save(student);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{classId}/import-students")
    public ResponseEntity<?> importStudents(@PathVariable Integer classId, @RequestParam("file") MultipartFile file) {
        try {
            ImportStudentsResult result = studentService.importStudentsFromExcel(classId, file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

    @PostMapping("/{classId}/remove-student")
    public ResponseEntity<?> removeStudentFromClass(@PathVariable Integer classId, @RequestBody AddStudentDTO dto) {
        Student student = studentService.getStudentById(dto.getStudentId());
        if (student == null) {
            return ResponseEntity.badRequest().body("学生不存在");
        }
        // 转移到“未分班”虚拟班级，避免 class_id 为 NULL 触发数据库约束
        Class unassigned = classService.getOrCreateUnassignedClass();
        student.setClazz(unassigned);
        studentService.save(student);
        return ResponseEntity.ok().build();
    }

    // 未分班学生列表
    @GetMapping("/unassigned/members")
    public ResponseEntity<List<StudentDTO>> getUnassignedStudents() {
        Class unassigned = classService.getOrCreateUnassignedClass();
        List<Student> students = studentService.findByClassId(unassigned.getId());
        List<StudentDTO> result = students.stream()
                .map(s -> new StudentDTO(
                        s.getId(),
                        s.getName(),
                        s.getStudentNo(),
                        s.getPhone(),          // 新增
                        s.getEmail()           // 新增
                ))
                .toList();
        return ResponseEntity.ok(result);
    }

    // 未分班学生数量
    @GetMapping("/unassigned/count")
    public ResponseEntity<Long> getUnassignedStudentCount() {
        Class unassigned = classService.getOrCreateUnassignedClass();
        long count = studentService.countByClassId(unassigned.getId());
        return ResponseEntity.ok(count);
    }
}
