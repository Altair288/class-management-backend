package com.altair288.class_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
    Throwable root = e.getRootCause();
    String msg = (root != null && root.getMessage() != null) ? root.getMessage() : e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            if (msg.contains("student_no")) {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "学号已存在");
            } else if (msg.contains("teacher_no")) {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "工号已存在");
            } else if (msg.contains("phone")) {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "手机号已存在");
            } else if (msg.contains("email")) {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "邮箱已存在");
            } else if (msg.contains("uk_credit_category") || msg.contains("category")) {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "该类别已存在配置项");
            } else if (msg.contains("uk_item_subitem")) {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "该主项目下已存在同名子项目");
            } else if (msg.contains("item_name")) {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "主项目名称已存在");
            } else {
                return error(HttpStatus.BAD_REQUEST, "DUPLICATE", "存在重复数据，请检查输入");
            }
        }
        return error(HttpStatus.BAD_REQUEST, "DATA_ERROR", "数据异常：" + msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_PARAM", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder("参数校验失败：");
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            sb.append(fe.getField()).append(" ").append(fe.getDefaultMessage()).append("; ");
        }
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", sb.toString());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", ex.getMessage());
    }
}