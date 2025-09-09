package com.altair288.class_management.repository;

import com.altair288.class_management.model.RoleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface RoleAssignmentRepository extends JpaRepository<RoleAssignment, Integer> {
    @Query("select r from RoleAssignment r where r.approvalRole.code = :roleCode and r.classId = :classId and r.enabled = true")
    Optional<RoleAssignment> findByRoleAndClass(@Param("roleCode") String roleCode, @Param("classId") Integer classId);

    @Query("select r from RoleAssignment r where r.approvalRole.code = :roleCode and r.departmentId = :departmentId and r.enabled = true")
    Optional<RoleAssignment> findByRoleAndDepartment(@Param("roleCode") String roleCode, @Param("departmentId") Integer departmentId);

    @Query("select r from RoleAssignment r where r.approvalRole.code = :roleCode and r.grade = :grade and r.enabled = true")
    Optional<RoleAssignment> findByRoleAndGrade(@Param("roleCode") String roleCode, @Param("grade") String grade);

    @Query("select r from RoleAssignment r where r.approvalRole.code = :roleCode and r.classId is null and r.departmentId is null and r.grade is null and r.enabled = true")
    Optional<RoleAssignment> findGlobalByRole(@Param("roleCode") String roleCode);

    java.util.List<RoleAssignment> findByTeacherId(Integer teacherId);
    java.util.List<RoleAssignment> findByTeacherIdIn(java.util.List<Integer> teacherIds);
}
