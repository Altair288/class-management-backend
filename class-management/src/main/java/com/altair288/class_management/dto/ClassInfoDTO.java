package com.altair288.class_management.dto;

public class ClassInfoDTO {
    private Integer id;
    private String name;
    private String grade; // 新增
    private String teacherName;
    private java.sql.Timestamp createdAt;

    public ClassInfoDTO(Integer id, String name, String grade, String teacherName, java.sql.Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.teacherName = teacherName;
        this.createdAt = createdAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}