package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "credit_subitem", uniqueConstraints = {
        @UniqueConstraint(name = "uk_item_subitem", columnNames = {"item_id", "subitem_name"})
})
public class CreditSubitem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 关联主项目（德/智/体/美/劳之一）
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private CreditItem item;

    @Column(name = "subitem_name", nullable = false, length = 100)
    private String subitemName;

    @Column(name = "initial_score", nullable = false)
    private Integer initialScore = 0; // 与现有代码一致用整数，底层可映射到 DECIMAL

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 100;

    @Column(name = "weight", nullable = false)
    private Double weight = 0d; // 配置用途，当前不参与计算

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt = new Date();

    @PreUpdate
    public void preUpdate() { this.updatedAt = new Date(); }

    // getters/setters
    public Integer getId() { return id; }
    public CreditItem getItem() { return item; }
    public void setItem(CreditItem item) { this.item = item; }
    public String getSubitemName() { return subitemName; }
    public void setSubitemName(String subitemName) { this.subitemName = subitemName; }
    public Integer getInitialScore() { return initialScore; }
    public void setInitialScore(Integer initialScore) { this.initialScore = initialScore; }
    public Integer getMaxScore() { return maxScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}