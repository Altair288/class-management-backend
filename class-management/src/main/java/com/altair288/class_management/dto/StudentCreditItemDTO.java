package com.altair288.class_management.dto;

public class StudentCreditItemDTO {
    private Integer itemId;
    private String category; // 德/智/体/美/劳
    private String itemName;
    private Double score;
    private Double maxScore;
    private Boolean enabled;
    private String description;

    public StudentCreditItemDTO() {}

    public StudentCreditItemDTO(Integer itemId, String category, String itemName, Double score, Double maxScore, Boolean enabled, String description) {
        this.itemId = itemId;
        this.category = category;
        this.itemName = itemName;
        this.score = score;
        this.maxScore = maxScore;
        this.enabled = enabled;
        this.description = description;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
