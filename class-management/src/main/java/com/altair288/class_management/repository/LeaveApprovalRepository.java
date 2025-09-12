package com.altair288.class_management.repository;

import com.altair288.class_management.model.LeaveApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import java.util.Collection;

public interface LeaveApprovalRepository extends JpaRepository<LeaveApproval, Integer> {
	@Query("select la from LeaveApproval la where la.leaveId = :leaveId and la.teacherId = :teacherId")
	Optional<LeaveApproval> findByLeaveIdAndTeacherId(@Param("leaveId") Integer leaveId, @Param("teacherId") Integer teacherId);

    @Query("select la from LeaveApproval la left join fetch la.approverRole r left join fetch la.teacher t where la.leaveId in :ids")
    List<LeaveApproval> findByLeaveIds(@Param("ids") Collection<Integer> ids);

	@Query("select la from LeaveApproval la where la.leaveId = :leaveId")
	List<LeaveApproval> findAllByLeaveId(@Param("leaveId") Integer leaveId);

	@Query("select la from LeaveApproval la where la.leaveId = :leaveId and la.status = '待审批'")
	List<LeaveApproval> findPendingByLeaveId(@Param("leaveId") Integer leaveId);
}
