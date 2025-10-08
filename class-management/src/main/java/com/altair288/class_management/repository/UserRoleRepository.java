package com.altair288.class_management.repository;

import com.altair288.class_management.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    List<UserRole> findByUserId(Integer userId);
    List<UserRole> findByRoleId(Integer roleId);

    // 查询某班当前班长(若有) —— 返回 user_id；后续可再查 student 信息
    @Query(value = "SELECT u.id FROM user_role ur " +
            "JOIN role r ON ur.role_id = r.id AND r.code='CLASS_MONITOR' " +
            "JOIN user u ON ur.user_id = u.id AND u.user_type='STUDENT' " +
            "JOIN student s ON u.related_id = s.id " +
            "WHERE s.class_id = :classId LIMIT 1", nativeQuery = true)
    Optional<Integer> findMonitorUserIdByClass(@Param("classId") Integer classId);

    // 删除某班全部班长绑定
    @Modifying
    @Transactional
    @Query(value = "DELETE ur FROM user_role ur " +
            "JOIN role r ON ur.role_id = r.id AND r.code='CLASS_MONITOR' " +
            "JOIN user u ON ur.user_id = u.id AND u.user_type='STUDENT' " +
            "JOIN student s ON u.related_id = s.id " +
            "WHERE s.class_id = :classId", nativeQuery = true)
    int deleteMonitorByClass(@Param("classId") Integer classId);
}