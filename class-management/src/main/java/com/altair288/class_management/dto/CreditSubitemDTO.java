package com.altair288.class_management.dto;

public class CreditSubitemDTO {
    private Integer id;
    private Integer itemId;      // 父 credit_item.id
    private String subitemName;
    private Integer initialScore;
    private Integer maxScore;
    private Double weight;
    private Boolean enabled;

    public CreditSubitemDTO() {}

    public CreditSubitemDTO(Integer id, Integer itemId, String subitemName,
                            Integer initialScore, Integer maxScore,
                            Double weight, Boolean enabled) {
        this.id = id;
        this.itemId = itemId;
        this.subitemName = subitemName;
        this.initialScore = initialScore;
        this.maxScore = maxScore;
        this.weight = weight;
        this.enabled = enabled;
    }

    public Integer getId() { return id; }
    public Integer getItemId() { return itemId; }
    public String getSubitemName() { return subitemName; }
    public Integer getInitialScore() { return initialScore; }
    public Integer getMaxScore() { return maxScore; }
    public Double getWeight() { return weight; }
    public Boolean getEnabled() { return enabled; }

    public void setId(Integer id) { this.id = id; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public void setSubitemName(String subitemName) { this.subitemName = subitemName; }
    public void setInitialScore(Integer initialScore) { this.initialScore = initialScore; }
    public void setMaxScore(Integer maxScore) { this.maxScore = maxScore; }
    public void setWeight(Double weight) { this.weight = weight; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}