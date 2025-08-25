package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "student_credit",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "credit_item_id"}))
public class StudentCredit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_item_id", nullable = false)
    private CreditItem creditItem;

    @Column(nullable = false)
    private Double score = 0.0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() { updatedAt = new Date(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = new Date(); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public CreditItem getCreditItem() { return creditItem; }
    public void setCreditItem(CreditItem creditItem) { this.creditItem = creditItem; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
