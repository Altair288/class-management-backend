package com.altair288.class_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String msg = e.getRootCause() != null ? e.getRootCause().getMessage() : e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            if (msg.contains("student_no")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("学号已存在");
            } else if (msg.contains("teacher_no")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("工号已存在");
            } else if (msg.contains("phone")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("手机号已存在");
            } else if (msg.contains("email")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("邮箱已存在");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("存在重复数据，请检查输入");
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("数据异常：" + msg);
    }

    // 你还可以加其他全局异常处理
    // 可选：处理其它异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOther(Exception ex) {
        Map<String, String> body = new HashMap<>();
        body.put("message", "服务器异常：" + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}