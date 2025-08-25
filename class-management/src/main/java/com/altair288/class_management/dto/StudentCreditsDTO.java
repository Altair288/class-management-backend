package com.altair288.class_management.dto;

public class StudentCreditsDTO {
    private Integer studentId;
    private String name;
    private String studentNo;
    private double de;   // 德
    private double zhi;  // 智
    private double ti;   // 体
    private double mei;  // 美
    private double lao;  // 劳

    public StudentCreditsDTO() {}

    public StudentCreditsDTO(Integer studentId, String name, String studentNo, double de, double zhi, double ti, double mei, double lao) {
        this.studentId = studentId;
        this.name = name;
        this.studentNo = studentNo;
        this.de = de;
        this.zhi = zhi;
        this.ti = ti;
        this.mei = mei;
        this.lao = lao;
    }

    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
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
}
