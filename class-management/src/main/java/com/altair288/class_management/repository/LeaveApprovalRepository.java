package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Integer> {
	@Query("select la from LeaveApproval la where la.leaveId = :leaveId and la.teacherId = :teacherId")
	Optional<LeaveApproval> findByLeaveIdAndTeacherId(@Param("leaveId") Integer leaveId, @Param("teacherId") Integer teacherId);
}
