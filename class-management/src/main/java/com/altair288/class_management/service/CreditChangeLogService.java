package com.altair288.class_management.service;

import com.altair288.class_management.model.CreditChangeLog;
import com.altair288.class_management.model.StudentCredit;
import com.altair288.class_management.repository.CreditChangeLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Service
public class CreditChangeLogService {

    private final CreditChangeLogRepository repository;

    public CreditChangeLogService(CreditChangeLogRepository repository) {
        this.repository = repository;
    }

    public static final class ActionType {
        public static final String DELTA = "DELTA";
        public static final String SET = "SET";
        public static final String RESET = "RESET";
        public static final String CLAMP = "CLAMP";
        public static final String INIT = "INIT";
        public static final String ROLLBACK = "ROLLBACK";
    }

    @Transactional
    public void logChange(Integer operatorUserId,
                          String operatorUsername,
                          String operatorRoleCodes,
                          StudentCredit sc,
                          double oldScore,
                          double newScore,
                          String actionType,
                          String reason,
                          String batchId,
                          String requestId,
                          boolean rollbackFlag) {
        CreditChangeLog log = new CreditChangeLog();
        log.setOperatorUserId(operatorUserId);
        log.setOperatorUsername(operatorUsername);
        log.setOperatorRoleCodes(operatorRoleCodes);
        log.setStudentId(sc.getStudent().getId());
        log.setStudentNo(sc.getStudent().getStudentNo());
        log.setStudentName(sc.getStudent().getName());
        log.setCreditItemId(sc.getCreditItem().getId());
        log.setCategory(sc.getCreditItem().getCategory());
        log.setItemName(sc.getCreditItem().getItemName());
    BigDecimal oldBD = BigDecimal.valueOf(oldScore).setScale(2, java.math.RoundingMode.HALF_UP);
    BigDecimal newBD = BigDecimal.valueOf(newScore).setScale(2, java.math.RoundingMode.HALF_UP);
    log.setOldScore(oldBD);
    log.setNewScore(newBD);
    log.setDelta(newBD.subtract(oldBD));
        log.setActionType(actionType);
        log.setReason(reason);
        log.setBatchId(batchId);
        log.setRequestId(requestId);
        log.setRollbackFlag(rollbackFlag);
        repository.save(log);
    }

    @Transactional
    public void batchLog(Integer operatorUserId,
                         String operatorUsername,
                         String operatorRoleCodes,
                         Collection<StudentCredit> credits,
                         List<Double> oldScores,
                         List<Double> newScores,
                         String actionType,
                         String reason,
                         String batchId) {
        int i = 0;
        for (StudentCredit sc : credits) {
            CreditChangeLog log = new CreditChangeLog();
            log.setOperatorUserId(operatorUserId);
            log.setOperatorUsername(operatorUsername);
            log.setOperatorRoleCodes(operatorRoleCodes);
            log.setStudentId(sc.getStudent().getId());
            log.setStudentNo(sc.getStudent().getStudentNo());
            log.setStudentName(sc.getStudent().getName());
            log.setCreditItemId(sc.getCreditItem().getId());
            log.setCategory(sc.getCreditItem().getCategory());
            log.setItemName(sc.getCreditItem().getItemName());
            double oldScore = oldScores.get(i);
            double newScore = newScores.get(i);
            BigDecimal oldBD = BigDecimal.valueOf(oldScore).setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal newBD = BigDecimal.valueOf(newScore).setScale(2, java.math.RoundingMode.HALF_UP);
            log.setOldScore(oldBD);
            log.setNewScore(newBD);
            log.setDelta(newBD.subtract(oldBD));
            log.setActionType(actionType);
            log.setReason(reason);
            log.setBatchId(batchId);
            log.setRollbackFlag(false);
            repository.save(log);
            i++;
        }
    }

    public Page<CreditChangeLog> search(Integer studentId,
                                        Integer itemId,
                                        String operatorUsername,
                                        String actionType,
                                        String batchId,
                                        Instant fromTime,
                                        Instant toTime,
                                        Pageable pageable) {
        return repository.search(studentId, itemId, operatorUsername, actionType, batchId, fromTime, toTime, pageable);
    }
}
