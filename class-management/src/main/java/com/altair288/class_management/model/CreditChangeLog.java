package com.altair288.class_management.model;

import jakarta.persistence.*;
import java.util.Date;
import java.math.BigDecimal;

/**
 * 学分变动日志实体，对应表 credit_change_log。
 */
@Entity
@Table(name = "credit_change_log")
public class CreditChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_user_id", nullable = false)
    private Integer operatorUserId;

    // 登录标识（学号/工号），新增列
    @Column(name = "operator_login", length = 100)
    private String operatorLogin;

    // 展示姓名（真实姓名），新增列；原有 operator_username 仍保留做兼容
    @Column(name = "operator_display_name", length = 100)
    private String operatorDisplayName;

    @Column(name = "operator_username", nullable = false, length = 100)
    private String operatorUsername;

    @Column(name = "operator_role_codes", length = 200)
    private String operatorRoleCodes; // 逗号分隔快照

    @Column(name = "student_id", nullable = false)
    private Integer studentId;

    @Column(name = "student_no", length = 20)
    private String studentNo;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "credit_item_id")
    private Integer creditItemId;

    @Column(name = "category", length = 10)
    private String category; // 快照（德/智/体/美/劳）

    @Column(name = "item_name", length = 100)
    private String itemName;

    @Column(name = "old_score", precision = 7, scale = 2)
    private BigDecimal oldScore;

    @Column(name = "new_score", precision = 7, scale = 2)
    private BigDecimal newScore;

    @Column(name = "delta", precision = 7, scale = 2)
    private BigDecimal delta;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "batch_id", length = 64)
    private String batchId;

    @Column(name = "request_id", length = 64)
    private String requestId;

    @Column(name = "rollback_flag", nullable = false)
    private Boolean rollbackFlag = false;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getOperatorUserId() { return operatorUserId; }
    public void setOperatorUserId(Integer operatorUserId) { this.operatorUserId = operatorUserId; }
    public String getOperatorLogin() { return operatorLogin; }
    public void setOperatorLogin(String operatorLogin) { this.operatorLogin = operatorLogin; }
    public String getOperatorDisplayName() { return operatorDisplayName; }
    public void setOperatorDisplayName(String operatorDisplayName) { this.operatorDisplayName = operatorDisplayName; }
    public String getOperatorUsername() { return operatorUsername; }
    public void setOperatorUsername(String operatorUsername) { this.operatorUsername = operatorUsername; }
    public String getOperatorRoleCodes() { return operatorRoleCodes; }
    public void setOperatorRoleCodes(String operatorRoleCodes) { this.operatorRoleCodes = operatorRoleCodes; }
    public Integer getStudentId() { return studentId; }
    public void setStudentId(Integer studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Integer getCreditItemId() { return creditItemId; }
    public void setCreditItemId(Integer creditItemId) { this.creditItemId = creditItemId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getOldScore() { return oldScore; }
    public void setOldScore(BigDecimal oldScore) { this.oldScore = oldScore; }
    public BigDecimal getNewScore() { return newScore; }
    public void setNewScore(BigDecimal newScore) { this.newScore = newScore; }
    public BigDecimal getDelta() { return delta; }
    public void setDelta(BigDecimal delta) { this.delta = delta; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public Boolean getRollbackFlag() { return rollbackFlag; }
    public void setRollbackFlag(Boolean rollbackFlag) { this.rollbackFlag = rollbackFlag; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
