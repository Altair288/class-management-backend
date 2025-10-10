package com.altair288.class_management.dto;

public class ChangePasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    public ChangePasswordDTO() {}

    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}
