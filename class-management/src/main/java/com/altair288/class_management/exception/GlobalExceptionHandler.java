package com.altair288.class_management.exception;

import com.altair288.class_management.ObjectStorage.dto.ApiError;
import com.altair288.class_management.ObjectStorage.exception.BadRequestException;
import com.altair288.class_management.ObjectStorage.exception.NotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

// NOTE: 已整合 ObjectStorage 模块的异常处理，不再需要其子包内的局部 GlobalExceptionHandler。
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---- 基础构建 ----
    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message, HttpServletRequest req){
        ApiError err = new ApiError(req.getRequestURI(), code, message);
        return ResponseEntity.status(status).body(err);
    }

    // ---- 数据唯一性 / 约束冲突 ----
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest req) {
        Throwable root = e.getRootCause();
        String msg = (root != null && root.getMessage() != null) ? root.getMessage() : e.getMessage();
        if (msg != null && msg.contains("Duplicate entry")) {
            if (msg.contains("student_no")) {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "学号已存在", req);
            } else if (msg.contains("teacher_no")) {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "工号已存在", req);
            } else if (msg.contains("phone")) {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "手机号已存在", req);
            } else if (msg.contains("email")) {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "邮箱已存在", req);
            } else if (msg.contains("uk_credit_category") || msg.contains("category")) {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "该类别已存在配置项", req);
            } else if (msg.contains("uk_item_subitem")) {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "该主项目下已存在同名子项目", req);
            } else if (msg.contains("item_name")) {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "主项目名称已存在", req);
            } else {
                return build(HttpStatus.BAD_REQUEST, "DUPLICATE", "存在重复数据，请检查输入", req);
            }
        }
        return build(HttpStatus.BAD_REQUEST, "DATA_ERROR", "数据异常：" + msg, req);
    }

    // ---- 参数非法 ----
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_PARAM", e.getMessage(), req);
    }

    // ---- 校验失败 ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        StringBuilder sb = new StringBuilder("参数校验失败：");
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            sb.append(fe.getField()).append(" ").append(fe.getDefaultMessage()).append("; ");
        }
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", sb.toString(), req);
    }

    // ---- 业务自定义：BadRequest ----
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException e, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, "BAD_REQUEST", e.getMessage(), req);
    }

    // ---- 业务自定义：NotFound ----
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException e, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", e.getMessage(), req);
    }

    // ---- 兜底 ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleOther(Exception ex, HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String ct = request.getContentType();
        if ((accept != null && accept.contains("text/event-stream")) || (ct != null && ct.contains("text/event-stream"))) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", ex.getMessage(), request);
    }
}