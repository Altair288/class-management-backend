package com.altair288.class_management.dto;

public class TeacherDTO {
    private Integer id;
    private String name;

    public TeacherDTO(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}