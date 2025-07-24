package com.altair288.class_management.dto;

public class ClassStudentCountDTO {
    private Integer classId;
    private String className;
    private Long studentCount;

    public ClassStudentCountDTO(Integer classId, String className, Long studentCount) {
        this.classId = classId;
        this.className = className;
        this.studentCount = studentCount;
    }

    public Integer getClassId() {
        return classId;
    }
    public void setClassId(Integer classId) {
        this.classId = classId;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }
    public Long getStudentCount() {
        return studentCount;
    }
    public void setStudentCount(Long studentCount) {
        this.studentCount = studentCount;
    }
}