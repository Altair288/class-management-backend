package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Date;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Integer> {
    List<LeaveRequest> findByStudentId(Integer studentId);
    List<LeaveRequest> findByTeacherId(Integer teacherId);
    List<LeaveRequest> findByStatus(String status);
    List<LeaveRequest> findByStudentIdAndStatus(Integer studentId, String status);
    List<LeaveRequest> findByTeacherIdAndStatus(Integer teacherId, String status);
    List<LeaveRequest> findByLeaveTypeId(Integer leaveTypeId);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.studentId = :studentId AND lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByStudentIdAndDateRange(@Param("studentId") Integer studentId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.studentId = :studentId AND lr.status = :status")
    Long countByStudentIdAndStatus(@Param("studentId") Integer studentId, @Param("status") String status);
    
    @Query("SELECT SUM(lr.days) FROM LeaveRequest lr WHERE lr.studentId = :studentId AND lr.leaveTypeId = :leaveTypeId AND lr.status = '已批准' AND YEAR(lr.startDate) = :year")
    Integer sumDaysByStudentIdAndLeaveTypeIdAndYear(@Param("studentId") Integer studentId, @Param("leaveTypeId") Integer leaveTypeId, @Param("year") Integer year);
}
