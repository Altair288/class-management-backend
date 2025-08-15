package com.altair288.class_management.dto;

import com.altair288.class_management.model.User.UserType;

public class UserDTO {
    private Integer id;
    private String username;
    private UserType userType;

    // 构造函数
    public UserDTO(Integer id, String username, UserType userType) {
        this.id = id;
        this.username = username;
        this.userType = userType;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
