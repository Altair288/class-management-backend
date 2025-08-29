package com.altair288.class_management.repository;

import com.altair288.class_management.model.StudentLeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface StudentLeaveBalanceRepository extends JpaRepository<StudentLeaveBalance, Integer> {
    List<StudentLeaveBalance> findByStudentId(Integer studentId);
    List<StudentLeaveBalance> findByStudentIdAndYear(Integer studentId, Integer year);
    Optional<StudentLeaveBalance> findByStudentIdAndLeaveTypeIdAndYear(Integer studentId, Integer leaveTypeId, Integer year);
    List<StudentLeaveBalance> findByLeaveTypeId(Integer leaveTypeId);
    List<StudentLeaveBalance> findByYear(Integer year);
    
    @Query("SELECT slb FROM StudentLeaveBalance slb WHERE slb.studentId = :studentId AND slb.year = :year")
    List<StudentLeaveBalance> findBalancesByStudentAndYear(@Param("studentId") Integer studentId, @Param("year") Integer year);
}
