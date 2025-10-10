package com.altair288.class_management.dto;

import jakarta.validation.constraints.Pattern;

/**
 * 更新个人资料（仅 phone/email，可部分更新；为空表示不修改或清空？这里选择：null = 不修改，空串 -> 清空）
 */
public class UpdateProfileDTO {

    // 大陆手机号：可选，以 1 开头的 11 位数字；允许 null/空串
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不合法")
    private String phone; // null 不修改；空串清空

    // 简单邮箱正则，允许空
    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不合法")
    private String email; // null 不修改；空串清空

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
