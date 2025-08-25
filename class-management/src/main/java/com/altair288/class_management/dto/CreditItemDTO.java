package com.altair288.class_management.dto;

public class CreditItemDTO {
    private Integer id;
    private String category; // 德/智/体/美/劳
    private String itemName;
    private Double initialScore;
    private Double maxScore;
    private Boolean enabled;
    private String description;

    public CreditItemDTO() {}

    public CreditItemDTO(Integer id, String category, String itemName, Double initialScore, Double maxScore, Boolean enabled, String description) {
        this.id = id;
        this.category = category;
        this.itemName = itemName;
        this.initialScore = initialScore;
        this.maxScore = maxScore;
        this.enabled = enabled;
        this.description = description;
    }

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
}
