package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "credit_item",
        uniqueConstraints = @UniqueConstraint(columnNames = {"category", "item_name"}))
public class CreditItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 保存中文类别：德/智/体/美/劳
    @Column(nullable = false, length = 10)
    private String category;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "initial_score")
    private Double initialScore;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        Date now = new Date();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = new Date(); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Double getInitialScore() { return initialScore; }
    public void setInitialScore(Double initialScore) { this.initialScore = initialScore; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}
