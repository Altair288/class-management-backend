// src/main/java/com/altair288/class_management/controller/UserController.java
package com.altair288.class_management.controller;

import com.altair288.class_management.dto.ClassSimpleDTO;
import com.altair288.class_management.dto.LoginRequestDTO;
import com.altair288.class_management.dto.UserDTO;
import com.altair288.class_management.dto.StudentRegisterDTO;
import com.altair288.class_management.dto.TeacherRegisterDTO;
import com.altair288.class_management.dto.ParentRegisterDTO;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


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
            // 验证用户凭据
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(), 
                    loginRequest.getPassword()
                )
            );

            // 设置认证信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息并返回
            User user = userService.getUserByUsername(loginRequest.getUsername());
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
    public ResponseEntity<UserDTO> registerStudent(@RequestBody StudentRegisterDTO dto) {
        Student student = new Student();
        student.setName(dto.getName());
        student.setStudentNo(dto.getStudentNo());
        student.setPhone(dto.getPhone());
        student.setEmail(dto.getEmail());

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
        user.setUsername(dto.getStudentNo());
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
    }

    @Transactional
    @PostMapping("/register/teacher")
    public ResponseEntity<UserDTO> registerTeacher(@RequestBody TeacherRegisterDTO dto) {
        Teacher teacher = new Teacher();
        teacher.setName(dto.getName());
        teacher.setTeacherNo(dto.getTeacherNo());
        teacher.setPhone(dto.getPhone());
        teacher.setEmail(dto.getEmail());
        teacher = teacherService.save(teacher);

        User user = new User();
        user.setUsername(dto.getTeacherNo());
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
    }

    @Transactional
    @PostMapping("/register/parent")
    public ResponseEntity<UserDTO> registerParent(@RequestBody ParentRegisterDTO dto) {
        Parent parent = new Parent();
        parent.setName(dto.getName());
        parent.setPhone(dto.getPhone());
        parent.setEmail(dto.getEmail());
        Student student = studentService.getStudentById(dto.getStudentId());
        parent.setStudent(student);
        parent = parentService.save(parent);

        User user = new User();
        user.setUsername(dto.getPhone());
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
    }

    @GetMapping("/student/count")
    public ResponseEntity<Long> getStudentCount() {
        long count = studentService.count();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/teacher/count")
    public ResponseEntity<Long> getTeacherCount() {
        long count = teacherService.count();
        return ResponseEntity.ok(count);
    }
    
    
    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        // 获取当前登录用户的信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(authentication.getName());
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
            .map(c -> new ClassSimpleDTO(c.getId(), c.getName()))
            .toList();
        return ResponseEntity.ok(result);
    }
}