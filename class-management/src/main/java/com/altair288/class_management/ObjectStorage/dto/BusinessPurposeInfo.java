package com.altair288.class_management.ObjectStorage.dto;

/**
 * 业务使用推荐的存储用途信息（非强制，仅用于前端指引与下拉选择）。
 */
public class BusinessPurposeInfo {
    private String code;           // 如 LEAVE_ATTACHMENT
    private String label;          // 展示名称（请假附件）
    private String description;    // 说明
    private String module;         // 所属业务模块（LEAVE / COMMON ...）
    private Boolean recommended;   // 是否为系统内置推荐

    public BusinessPurposeInfo() {}
    public BusinessPurposeInfo(String code, String label, String description, String module, Boolean recommended){
        this.code = code; this.label = label; this.description = description; this.module = module; this.recommended = recommended;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public Boolean getRecommended() { return recommended; }
    public void setRecommended(Boolean recommended) { this.recommended = recommended; }
}
