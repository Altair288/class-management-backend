package com.altair288.class_management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StudentCreditsViewDTO {
    private Integer id; // student id
    private String studentId; // studentNo
    private String studentName;
    @JsonProperty("class")
    private String className;
    @JsonProperty("德")
    private double de;
    @JsonProperty("智")
    private double zhi;
    @JsonProperty("体")
    private double ti;
    @JsonProperty("美")
    private double mei;
    @JsonProperty("劳")
    private double lao;
    private double total;
    private String status; // excellent/good/warning/danger

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public double getDe() { return de; }
    public void setDe(double de) { this.de = de; }
    public double getZhi() { return zhi; }
    public void setZhi(double zhi) { this.zhi = zhi; }
    public double getTi() { return ti; }
    public void setTi(double ti) { this.ti = ti; }
    public double getMei() { return mei; }
    public void setMei(double mei) { this.mei = mei; }
    public double getLao() { return lao; }
    public void setLao(double lao) { this.lao = lao; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
