package com.altair288.class_management.service;

import com.altair288.class_management.model.LeaveTypeConfig;
import com.altair288.class_management.repository.LeaveTypeConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class LeaveTypeConfigService {
    
    @Autowired
    private LeaveTypeConfigRepository leaveTypeConfigRepository;

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
        LeaveTypeConfig existing = getLeaveTypeById(id);
        if (existing != null) {
            existing.setEnabled(false);
            existing.setUpdatedAt(new Date());
            leaveTypeConfigRepository.save(existing);
        }
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
