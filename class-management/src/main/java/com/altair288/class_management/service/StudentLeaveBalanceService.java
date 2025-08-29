package com.altair288.class_management.service;

import com.altair288.class_management.model.StudentLeaveBalance;
import com.altair288.class_management.repository.StudentLeaveBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class StudentLeaveBalanceService {
    
    @Autowired
    private StudentLeaveBalanceRepository studentLeaveBalanceRepository;

    public List<StudentLeaveBalance> getStudentBalances(Integer studentId, Integer year) {
        if (year != null) {
            return studentLeaveBalanceRepository.findByStudentIdAndYear(studentId, year);
        } else {
            return studentLeaveBalanceRepository.findByStudentId(studentId);
        }
    }

    public StudentLeaveBalance getStudentBalance(Integer studentId, Integer leaveTypeId, Integer year) {
        Optional<StudentLeaveBalance> balance = studentLeaveBalanceRepository
            .findByStudentIdAndLeaveTypeIdAndYear(studentId, leaveTypeId, year);
        return balance.orElse(null);
    }

    public List<StudentLeaveBalance> getBalancesByLeaveType(Integer leaveTypeId) {
        return studentLeaveBalanceRepository.findByLeaveTypeId(leaveTypeId);
    }

    public List<StudentLeaveBalance> getBalancesByYear(Integer year) {
        return studentLeaveBalanceRepository.findByYear(year);
    }

    @Transactional
    public StudentLeaveBalance saveStudentBalance(StudentLeaveBalance balance) {
        if (balance.getId() == null) {
            balance.setUpdatedAt(new Date());
        }
        balance.setUpdatedAt(new Date());
        
        // 自动计算剩余天数
        if (balance.getTotalAllowance() != null && balance.getUsedDays() != null) {
            balance.setRemainingDays(balance.getTotalAllowance() - balance.getUsedDays());
        }
        
        return studentLeaveBalanceRepository.save(balance);
    }

    @Transactional
    public StudentLeaveBalance initializeStudentBalance(Integer studentId, Integer leaveTypeId, 
                                                       Integer year, Integer totalAllowance) {
        // 检查是否已存在
        Optional<StudentLeaveBalance> existing = studentLeaveBalanceRepository
            .findByStudentIdAndLeaveTypeIdAndYear(studentId, leaveTypeId, year);
        
        if (existing.isPresent()) {
            return existing.get();
        }
        
        // 创建新的余额记录
        StudentLeaveBalance balance = new StudentLeaveBalance();
        balance.setStudentId(studentId);
        balance.setLeaveTypeId(leaveTypeId);
        balance.setYear(year);
        balance.setTotalAllowance(totalAllowance);
        balance.setUsedDays(0.0);
        balance.setRemainingDays(totalAllowance.doubleValue());
        balance.setUpdatedAt(new Date());
        
        return studentLeaveBalanceRepository.save(balance);
    }

    @Transactional
    public void batchInitializeBalances(List<Integer> studentIds, Integer leaveTypeId, 
                                       Integer year, Integer totalAllowance) {
        for (Integer studentId : studentIds) {
            initializeStudentBalance(studentId, leaveTypeId, year, totalAllowance);
        }
    }

    @Transactional
    public StudentLeaveBalance updateBalance(Integer studentId, Integer leaveTypeId, 
                                           Integer year, Double usedDays) {
        Optional<StudentLeaveBalance> balanceOpt = studentLeaveBalanceRepository
            .findByStudentIdAndLeaveTypeIdAndYear(studentId, leaveTypeId, year);
        
        if (balanceOpt.isPresent()) {
            StudentLeaveBalance balance = balanceOpt.get();
            balance.setUsedDays(usedDays);
            balance.setRemainingDays(balance.getTotalAllowance() - usedDays);
            balance.setUpdatedAt(new Date());
            return studentLeaveBalanceRepository.save(balance);
        }
        
        return null;
    }

    @Transactional
    public StudentLeaveBalance resetBalance(Integer studentId, Integer leaveTypeId, Integer year) {
        Optional<StudentLeaveBalance> balanceOpt = studentLeaveBalanceRepository
            .findByStudentIdAndLeaveTypeIdAndYear(studentId, leaveTypeId, year);
        
        if (balanceOpt.isPresent()) {
            StudentLeaveBalance balance = balanceOpt.get();
            balance.setUsedDays(0.0);
            balance.setRemainingDays(balance.getTotalAllowance().doubleValue());
            balance.setUpdatedAt(new Date());
            return studentLeaveBalanceRepository.save(balance);
        }
        
        return null;
    }

    public List<StudentLeaveBalance> getAllBalances() {
        return studentLeaveBalanceRepository.findAll();
    }
}
