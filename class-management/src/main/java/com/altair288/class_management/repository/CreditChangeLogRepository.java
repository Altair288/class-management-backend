package com.altair288.class_management.repository;

import com.altair288.class_management.model.CreditChangeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface CreditChangeLogRepository extends JpaRepository<CreditChangeLog, Long> {

    @Query("select l from CreditChangeLog l where (:studentId is null or l.studentId = :studentId) " +
            "and (:itemId is null or l.creditItemId = :itemId) " +
            "and (:operatorUsername is null or l.operatorUsername = :operatorUsername) " +
            "and (:actionType is null or l.actionType = :actionType) " +
            "and (:batchId is null or l.batchId = :batchId) " +
            "and (:fromTime is null or l.createdAt >= :fromTime) " +
            "and (:toTime is null or l.createdAt <= :toTime)")
    Page<CreditChangeLog> search(@Param("studentId") Integer studentId,
                                 @Param("itemId") Integer itemId,
                                 @Param("operatorUsername") String operatorUsername,
                                 @Param("actionType") String actionType,
                                 @Param("batchId") String batchId,
                                 @Param("fromTime") Instant fromTime,
                                 @Param("toTime") Instant toTime,
                                 Pageable pageable);
}
