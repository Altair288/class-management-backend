package com.altair288.class_management.service;

import com.altair288.class_management.model.LeaveTypeConfig;
import com.altair288.class_management.repository.LeaveTypeConfigRepository;
import com.altair288.class_management.repository.LeaveRequestRepository;
import com.altair288.class_management.repository.StudentLeaveBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class LeaveTypeConfigService {
    
    @Autowired
    private LeaveTypeConfigRepository leaveTypeConfigRepository;
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private StudentLeaveBalanceRepository studentLeaveBalanceRepository;

    public List<LeaveTypeConfig> getAllActiveLeaveTypes() {
        return leaveTypeConfigRepository.findByEnabledTrueOrderByTypeCode();
    }

    public List<LeaveTypeConfig> getAllLeaveTypes() {
        return leaveTypeConfigRepository.findAll();
    }

    public LeaveTypeConfig getLeaveTypeById(Integer id) {
        return leaveTypeConfigRepository.findById(id).orElse(null);
    }

    public LeaveTypeConfig getLeaveTypeByName(String typeName) {
        return leaveTypeConfigRepository.findByTypeName(typeName);
    }

    public LeaveTypeConfig getLeaveTypeByCode(String typeCode) {
        return leaveTypeConfigRepository.findByTypeCode(typeCode);
    }

    public LeaveTypeConfig saveLeaveType(LeaveTypeConfig leaveTypeConfig) {
        if (leaveTypeConfig.getId() == null) {
            leaveTypeConfig.setCreatedAt(new Date());
        }
        leaveTypeConfig.setUpdatedAt(new Date());
        return leaveTypeConfigRepository.save(leaveTypeConfig);
    }

    public void deleteLeaveType(Integer id) {
        // 检查是否有关联的请假申请
        Long leaveRequestCount = leaveRequestRepository.countByLeaveTypeId(id);
        if (leaveRequestCount > 0) {
            throw new RuntimeException("无法删除该请假类型，因为已有 " + leaveRequestCount + " 条相关的请假申请");
        }
        
        // 检查是否有关联的学生请假余额记录
        Long balanceCount = studentLeaveBalanceRepository.countByLeaveTypeId(id);
        if (balanceCount > 0) {
            throw new RuntimeException("无法删除该请假类型，因为已有 " + balanceCount + " 条相关的学生请假余额记录");
        }
        
        // 如果没有关联数据，则可以安全删除
        leaveTypeConfigRepository.deleteById(id);
    }

    public LeaveTypeConfig activateLeaveType(Integer id) {
        LeaveTypeConfig existing = getLeaveTypeById(id);
        if (existing != null) {
            existing.setEnabled(true);
            existing.setUpdatedAt(new Date());
            return leaveTypeConfigRepository.save(existing);
        }
        return null;
    }

    public LeaveTypeConfig deactivateLeaveType(Integer id) {
        LeaveTypeConfig existing = getLeaveTypeById(id);
        if (existing != null) {
            existing.setEnabled(false);
            existing.setUpdatedAt(new Date());
            return leaveTypeConfigRepository.save(existing);
        }
        return null;
    }
}
