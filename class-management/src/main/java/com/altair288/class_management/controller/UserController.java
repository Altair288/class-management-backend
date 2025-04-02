// src/main/java/com/altair288/class_management/controller/UserController.java
package com.altair288.class_management.controller;

import com.altair288.class_management.dto.UserDTO;
import com.altair288.class_management.model.User;
import com.altair288.class_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
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