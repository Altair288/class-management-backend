// src/main/java/com/altair288/class_management/controller/UserController.java
package com.altair288.class_management.controller;

import com.altair288.class_management.dto.LoginRequestDTO;
import com.altair288.class_management.dto.UserDTO;
import com.altair288.class_management.model.User;
import com.altair288.class_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
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
        User registeredUser = userService.registerUser(user);
        UserDTO userDTO = new UserDTO(
            registeredUser.getId(),
            registeredUser.getUsername(),
            registeredUser.getUserType()
        );
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
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