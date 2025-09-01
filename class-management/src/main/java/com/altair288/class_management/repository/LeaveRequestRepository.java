package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Date;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByStudentId(Integer studentId);
    List<LeaveRequest> findByStatus(String status);
    List<LeaveRequest> findByStudentIdAndStatus(Integer studentId, String status);
    List<LeaveRequest> findByLeaveTypeId(Integer leaveTypeId);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.studentId = :studentId AND lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByStudentIdAndDateRange(@Param("studentId") Integer studentId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.leaveTypeId = :leaveTypeId")
    Long countByLeaveTypeId(@Param("leaveTypeId") Integer leaveTypeId);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.studentId = :studentId AND lr.status = :status")
    Long countByStudentIdAndStatus(@Param("studentId") Integer studentId, @Param("status") String status);
    
    @Query("SELECT SUM(lr.days) FROM LeaveRequest lr WHERE lr.studentId = :studentId AND lr.leaveTypeId = :leaveTypeId AND lr.status = '已批准' AND YEAR(lr.startDate) = :year")
    Integer sumDaysByStudentIdAndLeaveTypeIdAndYear(@Param("studentId") Integer studentId, @Param("leaveTypeId") Integer leaveTypeId, @Param("year") Integer year);

    // 日历视图批量查询：一次性连表拿到所需轻量字段，避免 N+1
    interface CalendarProjection {
        Integer getId();
        Integer getStudentId();
        String getStudentName();
        String getStudentNo();
        String getLeaveTypeCode();
        String getLeaveTypeName();
        String getStatus();
        Date getStartDate();
        Date getEndDate();
    }

    @Query("select lr.id as id, s.id as studentId, s.name as studentName, s.studentNo as studentNo, lt.typeCode as leaveTypeCode, lt.typeName as leaveTypeName, lr.status as status, lr.startDate as startDate, lr.endDate as endDate " +
           "from LeaveRequest lr join Student s on s.id = lr.studentId join LeaveTypeConfig lt on lt.id = lr.leaveTypeId " +
           "where (:classId is null or s.clazz.id = :classId) " +
           "and (:status is null or lr.status = :status) " +
           "and (:start is null or lr.endDate >= :start) " +
           "and (:end is null or lr.startDate <= :end)")
    List<CalendarProjection> findForCalendar(@Param("classId") Integer classId,
                                             @Param("status") String status,
                                             @Param("start") Date start,
                                             @Param("end") Date end);

    // 通过审批表按教师查询其关联的请假单（单级审批，一单一记录）
    @Query("select lr from LeaveRequest lr where exists (select 1 from LeaveApproval la where la.leaveId = lr.id and la.teacherId = :teacherId)")
    List<LeaveRequest> findByApprover(@Param("teacherId") Integer teacherId);

    // 通过审批表按教师+状态查询
    @Query("select lr from LeaveRequest lr where lr.status = :status and exists (select 1 from LeaveApproval la where la.leaveId = lr.id and la.teacherId = :teacherId)")
    List<LeaveRequest> findByApproverAndStatus(@Param("teacherId") Integer teacherId, @Param("status") String status);
}
