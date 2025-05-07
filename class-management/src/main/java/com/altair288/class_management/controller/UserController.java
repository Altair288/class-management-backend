// src/main/java/com/altair288/class_management/controller/UserController.java
package com.altair288.class_management.controller;

import com.altair288.class_management.dto.LoginRequestDTO;
import com.altair288.class_management.dto.UserDTO;
import com.altair288.class_management.model.User;
import com.altair288.class_management.model.Student;
import com.altair288.class_management.model.Parent;
import com.altair288.class_management.model.Teacher;
import com.altair288.class_management.service.UserService;
import com.altair288.class_management.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final StudentService studentService;
    private final TeacherService teacherService;
    private final ParentService parentService;

    public UserController(UserService userService, AuthenticationManager authenticationManager, StudentService studentService, 
                          TeacherService teacherService, ParentService parentService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager; // Initialize authenticationManager
        this.studentService = studentService; // Initialize studentService
        this.teacherService = teacherService; // Initialize teacherService
        this.parentService = parentService; // Initialize parentService
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

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody User user) {
        // 1. 根据userType自动设置username
        if (user.getUserType() == User.UserType.STUDENT && user.getRelatedId() != null) {
            Student student = studentService.getStudentById(user.getRelatedId());
            if (student == null) {
                throw new IllegalArgumentException("学生ID不存在");
            }
            user.setUsername(student.getStudentNo());
        } else if (user.getUserType() == User.UserType.TEACHER && user.getRelatedId() != null) {
            Teacher teacher = teacherService.getTeacherById(user.getRelatedId());
            if (teacher == null) {
                throw new IllegalArgumentException("教师ID不存在");
            }
            user.setUsername(teacher.getTeacherNo());
        } else if (user.getUserType() == User.UserType.PARENT && user.getRelatedId() != null) {
            Parent parent = parentService.getParentById(user.getRelatedId());
            if (parent == null) {
                throw new IllegalArgumentException("家长ID不存在");
            }
            user.setUsername(parent.getPhone());
        }
        User registeredUser = userService.registerUser(user);
        UserDTO userDTO = new UserDTO(
            registeredUser.getId(),
            registeredUser.getUsername(),
            registeredUser.getUserType()
        );
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
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
}