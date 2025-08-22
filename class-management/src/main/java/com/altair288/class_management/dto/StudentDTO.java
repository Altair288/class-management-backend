package com.altair288.class_management.dto;

public class StudentDTO {
    private Integer id;
    private String name;
    private String studentNo;
    private String phone;   // 新增
    private String email;   // 新增

    // 新增完整构造方法
    public StudentDTO(Integer id, String name, String studentNo, String phone, String email) {
        this.id = id;
        this.name = name;
        this.studentNo = studentNo;
        this.phone = phone;
        this.email = email;
    }

    // 兼容旧用法（不传手机/邮箱）
    public StudentDTO(Integer id, String name, String studentNo) {
        this(id, name, studentNo, null, null);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getStudentNo() { return studentNo; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
}