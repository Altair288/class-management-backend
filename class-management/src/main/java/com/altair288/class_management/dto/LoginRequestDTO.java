package com.altair288.class_management.dto;

public class LoginRequestDTO {
    private String username;
    private String password;
    
    public String getUsername() {
        return username;
    }
    public void getUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
}
