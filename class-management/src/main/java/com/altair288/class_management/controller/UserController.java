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
import com.altair288.class_management.service.RoleService;
import com.altair288.class_management.service.UserRoleService;
import com.altair288.class_management.model.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import com.altair288.class_management.dto.TeacherDTO;

import java.util.List;


@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ParentService parentService;
    private final ClassService classService;
    private final RoleService roleService;
    private final UserRoleService userRoleService;

    public UserController(UserService userService, StudentService studentService, 
                          TeacherService teacherService, ParentService parentService, ClassService classService,
                          RoleService roleService, UserRoleService userRoleService) {
        this.userService = userService;
        this.studentService = studentService; // Initialize studentService
        this.teacherService = teacherService; // Initialize teacherService
        this.parentService = parentService; // Initialize parentService
        this.classService = classService; // Initialize classService
        this.roleService = roleService;
        this.userRoleService = userRoleService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest) {
        try {
            User user = userService.getUserByUsernameOrIdentityNo(loginRequest.getUsername());
            // 密码校验
            if (!userService.getPasswordEncoder().matches(loginRequest.getPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("密码错误");
            }
            UserDTO userDTO = buildUserDTOWithMonitorInfo(user);
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

            // 赋予学生基础角色（若未已有）
            try {
                Role studentRole = roleService.getByCode(Role.Codes.STUDENT);
                userRoleService.assignRoleToUser(registeredUser.getId(), studentRole.getId());
            } catch (Exception ignored) {}

            UserDTO userDTO = new UserDTO(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getUserType());
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

            // 赋予教师基础角色
            try {
                Role teacherRole = roleService.getByCode(Role.Codes.TEACHER);
                userRoleService.assignRoleToUser(registeredUser.getId(), teacherRole.getId());
            } catch (Exception ignored) {}

            UserDTO userDTO = new UserDTO(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getUserType());
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

            // 赋予家长基础角色
            try {
                Role parentRole = roleService.getByCode(Role.Codes.PARENT);
                userRoleService.assignRoleToUser(registeredUser.getId(), parentRole.getId());
            } catch (Exception ignored) {}

            UserDTO userDTO = new UserDTO(registeredUser.getId(), registeredUser.getUsername(), registeredUser.getUserType());
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
        UserDTO userDTO = buildUserDTOWithMonitorInfo(user);
        return ResponseEntity.ok(userDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        UserDTO userDTO = buildUserDTOWithMonitorInfo(user);
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

    // ========== 内部工具方法：构建含班长标记的 UserDTO ==========
    private UserDTO buildUserDTOWithMonitorInfo(User user) {
        UserDTO dto = new UserDTO(user.getId(), user.getUsername(), user.getUserType());
        try {
            if (user.getUserType() == User.UserType.STUDENT) {
                // 判断是否拥有 CLASS_MONITOR 角色
                boolean isMonitor = userService.userHasRoleCode(user.getId(), com.altair288.class_management.model.Role.Codes.CLASS_MONITOR);
                dto.setClassMonitor(isMonitor);
                if (isMonitor && user.getRelatedId() != null) {
                    // 取该学生班级ID
                    var student = studentService.getStudentById(user.getRelatedId());
                    if (student != null && student.getClazz() != null) {
                        dto.setMonitorClassId(student.getClazz().getId());
                    }
                }
            } else {
                dto.setClassMonitor(false);
            }
        } catch (Exception ignored) {
            // 出现异常不影响主流程
        }
        return dto;
    }
}