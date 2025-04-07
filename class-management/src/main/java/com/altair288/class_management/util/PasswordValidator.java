package com.altair288.class_management.util;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 20;
    // 修正正则表达式
    private static final Pattern HAS_UPPER = Pattern.compile(".*[A-Z].*"); // 修改为大写字母匹配
    private static final Pattern HAS_LOWER = Pattern.compile(".*[a-z].*"); // 修改为小写字母匹配
    private static final Pattern HAS_NUMBER = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*");

    public static void validatePassword(String password) {
        if (password == null || password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("密码长度必须在8-20位之间");
        }
        if (!HAS_UPPER.matcher(password).find()) {
            throw new IllegalArgumentException("密码必须包含大写字母");
        }
        if (!HAS_LOWER.matcher(password).find()) {
            throw new IllegalArgumentException("密码必须包含小写字母");
        }
        if (!HAS_NUMBER.matcher(password).find()) {
            throw new IllegalArgumentException("密码必须包含数字");
        }
        if (!HAS_SPECIAL.matcher(password).find()) {
            throw new IllegalArgumentException("密码必须包含特殊字符");
        }
    }
}
