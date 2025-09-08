package com.altair288.class_management.dto;

/**
 * 前端班级管理列表 DTO
 * 包含：id, name, grade, department(id+name), teacher(id+name)
 */
public class ClassListDTO {
    private Integer id;
    private String name;
    private String grade;
    private Integer departmentId;
    private String departmentName;
    private Integer teacherId;
    private String teacherName;

    public ClassListDTO(Integer id, String name, String grade, Integer departmentId, String departmentName, Integer teacherId, String teacherName) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getGrade() { return grade; }
    public Integer getDepartmentId() { return departmentId; }
    public String getDepartmentName() { return departmentName; }
    public Integer getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
}
