package com.altair288.class_management.controller;

import com.altair288.class_management.dto.ClassInfoDTO;
import com.altair288.class_management.dto.ClassSimpleDTO;
import com.altair288.class_management.dto.ClassStudentCountDTO;
import com.altair288.class_management.dto.CreateClassDTO;
import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Teacher;
import com.altair288.class_management.service.ClassService;
import com.altair288.class_management.service.StudentService;
import com.altair288.class_management.service.TeacherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;


@RestController
@RequestMapping("/api/class")
public class ClassController {

    private final ClassService classService;
    private final StudentService studentService;
    private final TeacherService teacherService;

    public ClassController(ClassService classService, StudentService studentService, TeacherService teacherService) {
        this.classService = classService;
        this.studentService = studentService;
        this.teacherService = teacherService;
    }

    // 查询所有班级详细信息
    @GetMapping("/all")
    public ResponseEntity<List<ClassInfoDTO>> getAllClasses() {
        List<Class> classes = classService.findAll();
        List<ClassInfoDTO> result = classes.stream().map(c -> {
            String teacherName = c.getTeacher() != null ? c.getTeacher().getName() : null;
            // createdAt 类型转换
            Timestamp createdAt = c.getCreatedAt() == null ? null : new Timestamp(c.getCreatedAt().getTime());
            return new ClassInfoDTO(
                c.getId(),
                c.getName(),
                c.getGrade(), // 新增
                teacherName,
                createdAt
            );
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
        Class clazz = new Class();
        clazz.setName(dto.getName());
        clazz.setGrade(dto.getGrade());
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherService.getById(dto.getTeacherId());
            clazz.setTeacher(teacher);
        }
        classService.save(clazz);
        return ResponseEntity.ok().build();
    }
}
