package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "leave_type_config")
public class LeaveTypeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "type_code", nullable = false, unique = true)
    private String typeCode;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "description")
    private String description;

    @Column(name = "max_days_per_request", nullable = false)
    private Integer maxDaysPerRequest = 30;

    @Column(name = "annual_allowance", nullable = false)
    private Integer annualAllowance = 15;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = true;

    @Column(name = "requires_medical_proof", nullable = false)
    private Boolean requiresMedicalProof = false;

    @Column(name = "advance_days_required", nullable = false)
    private Integer advanceDaysRequired = 1;

    @Column(name = "color", nullable = false)
    private String color = "#1976d2";

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getMaxDaysPerRequest() {
        return maxDaysPerRequest;
    }

    public void setMaxDaysPerRequest(Integer maxDaysPerRequest) {
        this.maxDaysPerRequest = maxDaysPerRequest;
    }

    public Integer getAnnualAllowance() {
        return annualAllowance;
    }

    public void setAnnualAllowance(Integer annualAllowance) {
        this.annualAllowance = annualAllowance;
    }

    public Boolean getRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(Boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
    }

    public Boolean getRequiresMedicalProof() {
        return requiresMedicalProof;
    }

    public void setRequiresMedicalProof(Boolean requiresMedicalProof) {
        this.requiresMedicalProof = requiresMedicalProof;
    }

    public Integer getAdvanceDaysRequired() {
        return advanceDaysRequired;
    }

    public void setAdvanceDaysRequired(Integer advanceDaysRequired) {
        this.advanceDaysRequired = advanceDaysRequired;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
