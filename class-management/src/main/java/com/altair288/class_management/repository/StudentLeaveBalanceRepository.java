package com.altair288.class_management.repository;

import com.altair288.class_management.model.StudentLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StudentLeaveBalanceRepository extends JpaRepository<StudentLeaveBalance, Integer> {
    List<StudentLeaveBalance> findByStudentId(Integer studentId);
    List<StudentLeaveBalance> findByStudentIdAndYear(Integer studentId, Integer year);
    Optional<StudentLeaveBalance> findByStudentIdAndLeaveTypeIdAndYear(Integer studentId, Integer leaveTypeId, Integer year);
    List<StudentLeaveBalance> findByLeaveTypeId(Integer leaveTypeId);
    List<StudentLeaveBalance> findByYear(Integer year);
    
    @Query("SELECT COUNT(slb) FROM StudentLeaveBalance slb WHERE slb.leaveTypeId = :leaveTypeId")
    Long countByLeaveTypeId(@Param("leaveTypeId") Integer leaveTypeId);
    
    @Query("SELECT slb FROM StudentLeaveBalance slb WHERE slb.studentId = :studentId AND slb.year = :year")
    List<StudentLeaveBalance> findBalancesByStudentAndYear(@Param("studentId") Integer studentId, @Param("year") Integer year);

    // 批量更新：当某类型的年度额度变更后，批量同步所有年份的余额（remainingDays 按使用天数重算并且不为负）
    @Modifying
    @Query("update StudentLeaveBalance slb set slb.totalAllowance = :newAllowance, " +
        "slb.remainingDays = case when (:newAllowance - coalesce(slb.usedDays, 0.0)) < 0 then 0.0 else (:newAllowance - coalesce(slb.usedDays, 0.0)) end, " +
        "slb.updatedAt = current_timestamp where slb.leaveTypeId = :leaveTypeId")
    int bulkUpdateAllYearsByType(@Param("leaveTypeId") Integer leaveTypeId, @Param("newAllowance") Integer newAllowance);

    // 批量更新：仅更新某一学年的余额
    @Modifying
    @Query("update StudentLeaveBalance slb set slb.totalAllowance = :newAllowance, " +
        "slb.remainingDays = case when (:newAllowance - coalesce(slb.usedDays, 0.0)) < 0 then 0.0 else (:newAllowance - coalesce(slb.usedDays, 0.0)) end, " +
        "slb.updatedAt = current_timestamp where slb.leaveTypeId = :leaveTypeId and slb.year = :year")
    int bulkUpdateByTypeAndYear(@Param("leaveTypeId") Integer leaveTypeId, @Param("year") Integer year, @Param("newAllowance") Integer newAllowance);
}
