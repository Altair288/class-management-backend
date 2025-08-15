package com.altair288.class_management.dto;

public class StudentDTO {
    private Integer id;
    private String name;
    private String studentNo;

    public StudentDTO(Integer id, String name, String studentNo) {
        this.id = id;
        this.name = name;
        this.studentNo = studentNo;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStudentNo() {
        return studentNo;
    }
}