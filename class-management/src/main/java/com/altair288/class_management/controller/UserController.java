// src/main/java/com/altair288/class_management/controller/UserController.java
package com.altair288.class_management.controller;

import com.altair288.class_management.dto.ClassSimpleDTO;
import com.altair288.class_management.dto.LoginRequestDTO;
import com.altair288.class_management.dto.UserDTO;
import com.altair288.class_management.dto.StudentRegisterDTO;
import com.altair288.class_management.dto.TeacherRegisterDTO;
import com.altair288.class_management.dto.ParentRegisterDTO;
import com.altair288.class_management.dto.StudentDTO;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.Parent;
import com.altair288.class_management.model.Teacher;
import com.altair288.class_management.service.UserService;
import com.altair288.class_management.service.StudentService;
import com.altair288.class_management.service.TeacherService;
import com.altair288.class_management.service.ParentService;
import com.altair288.class_management.service.ClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import com.altair288.class_management.dto.TeacherDTO;
import com.altair288.class_management.model.Teacher;
import com.altair288.class_management.service.TeacherService;

import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ParentService parentService;
    private final ClassService classService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, StudentService studentService, 
                          TeacherService teacherService, ParentService parentService, ClassService classService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager; // Initialize authenticationManager
        this.studentService = studentService; // Initialize studentService
        this.teacherService = teacherService; // Initialize teacherService
        this.parentService = parentService; // Initialize parentService
        this.classService = classService; // Initialize classService
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            User user = userService.getUserByUsernameOrIdentityNo(loginRequest.getUsername());
            // 密码校验
            if (!userService.getPasswordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("密码错误");
            }
            UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getUserType()
            );
            return ResponseEntity.ok(userDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("登录失败：" + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@RequestBody StudentRegisterDTO dto) {
        try {
            Student student = new Student();
            student.setName(dto.getName());
            student.setStudentNo(dto.getStudentNo());
            student.setPhone(dto.getPhone() == null || dto.getPhone().isBlank() ? null : dto.getPhone());
            student.setEmail(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail());

            com.altair288.class_management.model.Class clazz = null;
            if (dto.getClassId() != null) {
                clazz = classService.getById(dto.getClassId());
            } else if (dto.getClassName() != null && !dto.getClassName().isEmpty()) {
                clazz = classService.getByName(dto.getClassName());
            }
            if (clazz == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            student.setClazz(clazz);

            student = studentService.save(student);

            User user = new User();
            user.setUsername(dto.getName());
            user.setIdentityNo(dto.getStudentNo());
            user.setPassword(dto.getPassword());
            user.setUserType(User.UserType.STUDENT);
            user.setRelatedId(student.getId());
            User registeredUser = userService.registerUser(user);

            UserDTO userDTO = new UserDTO(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getUserType()
            );
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw e; // 直接抛出异常，让全局异常处理器处理
            //return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } /* catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败：" + e.getMessage());
        } */
    }

    @Transactional
    @PostMapping("/register/teacher")
    public ResponseEntity<?> registerTeacher(@RequestBody TeacherRegisterDTO dto) {
        try {
            Teacher teacher = new Teacher();
            teacher.setName(dto.getName());
            teacher.setTeacherNo(dto.getTeacherNo());
            teacher.setPhone(dto.getPhone() == null || dto.getPhone().isBlank() ? null : dto.getPhone());
            teacher.setEmail(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail());
            teacher = teacherService.save(teacher);

            // if (true) throw new RuntimeException("测试事务回滚");

            User user = new User();
            user.setUsername(dto.getName());
            user.setIdentityNo(dto.getTeacherNo());
            user.setPassword(dto.getPassword());
            user.setUserType(User.UserType.TEACHER);
            user.setRelatedId(teacher.getId());
            User registeredUser = userService.registerUser(user);

            UserDTO userDTO = new UserDTO(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getUserType()
            );
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw e; // 直接抛出异常，让全局异常处理器处理
            // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } /* catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败：" + e.getMessage());
        }  */       
    }

    @Transactional
    @PostMapping("/register/parent")
    public ResponseEntity<?> registerParent(@RequestBody ParentRegisterDTO dto) {
        try {
            Parent parent = new Parent();
            parent.setName(dto.getName());
            parent.setPhone(dto.getPhone() == null || dto.getPhone().isBlank() ? null : dto.getPhone());
            parent.setEmail(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail());
            Student student = studentService.getStudentByStudentNo(dto.getStudentNo());
            parent.setStudent(student);
            parent = parentService.save(parent);

            User user = new User();
            user.setUsername(dto.getName());
            user.setIdentityNo(dto.getPhone());
            user.setPassword(dto.getPassword());
            user.setUserType(User.UserType.PARENT);
            user.setRelatedId(parent.getId());
            User registeredUser = userService.registerUser(user);

            UserDTO userDTO = new UserDTO(
                registeredUser.getId(),
                registeredUser.getUsername(),
                registeredUser.getUserType()
            );
            return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw e; // 直接抛出异常，让全局异常处理器处理
            // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } /* catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("注册失败：" + e.getMessage());
        } */
    }

    @GetMapping("/student/count")
    public ResponseEntity<Long> getStudentCount() {
        long count = studentService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/student/by-no")
    public ResponseEntity<?> getStudentByNo(@RequestParam String studentNo) {
        Student student = studentService.getStudentByStudentNo(studentNo);
        if (student == null) {
            return ResponseEntity.status(404).body("未找到该学号");
        }
        StudentDTO dto = new StudentDTO(student.getId(), student.getName(), student.getStudentNo());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/teacher/count")
    public ResponseEntity<Long> getTeacherCount() {
        long count = teacherService.count();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/teacher/all")
    public ResponseEntity<List<TeacherDTO>> getAllTeachers() {
        List<Teacher> teachers = teacherService.findAll();
        List<TeacherDTO> result = teachers.stream()
            .map(t -> new TeacherDTO(t.getId(), t.getName()))
            .toList();
        return ResponseEntity.ok(result);
    }


    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        // 获取当前登录用户的信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsernameOrIdentityNo(authentication.getName());
        UserDTO userDTO = new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getUserType()
        );
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        UserDTO userDTO = new UserDTO(
            user.getId(),
            user.getUsername(),
            user.getUserType()
        );
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/classes")
    public ResponseEntity<?> getAllClasses() {
        var classes = classService.findAll();
        var result = classes.stream()
            .map(c -> new ClassSimpleDTO(c.getId(), c.getName(), c.getGrade()))
            .toList();
        return ResponseEntity.ok(result);
    }
}