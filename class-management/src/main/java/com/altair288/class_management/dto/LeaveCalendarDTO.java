package com.altair288.class_management.dto;

import java.util.Date;

/**
 * 日历视图专用 DTO：只包含必要字段，避免 N+1
 */
public class LeaveCalendarDTO {
    private Integer requestId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String leaveTypeCode;
    private String leaveTypeName;
    private String status;
    private Date startDate;
    private Date endDate;

    public LeaveCalendarDTO(Integer requestId,
                             Integer studentId,
                             String studentName,
                             String studentNo,
                             String leaveTypeCode,
                             String leaveTypeName,
                             String status,
                             Date startDate,
                             Date endDate) {
        this.requestId = requestId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentNo = studentNo;
        this.leaveTypeCode = leaveTypeCode;
        this.leaveTypeName = leaveTypeName;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Integer getRequestId() { return requestId; }
    public Integer getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentNo() { return studentNo; }
    public String getLeaveTypeCode() { return leaveTypeCode; }
    public String getLeaveTypeName() { return leaveTypeName; }
    public String getStatus() { return status; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
}
