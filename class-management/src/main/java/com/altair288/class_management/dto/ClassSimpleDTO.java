package com.altair288.class_management.dto;

public class ClassSimpleDTO {
    private Integer id;
    private String name;
    private String grade; // 新增

    public ClassSimpleDTO(Integer id, String name, String grade) {
        this.id = id;
        this.name = name;
        this.grade = grade;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
}
