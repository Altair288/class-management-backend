package com.altair288.class_management.service;

import com.altair288.class_management.model.LeaveTypeConfig;
import com.altair288.class_management.model.StudentLeaveBalance;
import com.altair288.class_management.repository.LeaveTypeConfigRepository;
import com.altair288.class_management.repository.LeaveRequestRepository;
import com.altair288.class_management.repository.StudentLeaveBalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Calendar;
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

    @Transactional
    public LeaveTypeConfig saveLeaveType(LeaveTypeConfig leaveTypeConfig) {
        LeaveTypeConfig oldConfig = null;
        boolean isUpdate = leaveTypeConfig.getId() != null;
        
        if (isUpdate) {
            // 获取更新前的配置
            oldConfig = getLeaveTypeById(leaveTypeConfig.getId());
        }
        
        if (leaveTypeConfig.getId() == null) {
            leaveTypeConfig.setCreatedAt(new Date());
        }
        leaveTypeConfig.setUpdatedAt(new Date());
        
        // 保存配置
        LeaveTypeConfig savedConfig = leaveTypeConfigRepository.save(leaveTypeConfig);
        
        // 如果是更新操作，需要同步更新相关数据
        if (isUpdate && oldConfig != null) {
            updateRelatedDataAfterConfigChange(oldConfig, savedConfig);
        }
        
        return savedConfig;
    }
    
    /**
     * 配置更新后同步更新相关数据
     */
    @Transactional
    private void updateRelatedDataAfterConfigChange(LeaveTypeConfig oldConfig, LeaveTypeConfig newConfig) {
        // 1. 如果年度额度发生变化，更新所有学生的年度余额
        if (!oldConfig.getAnnualAllowance().equals(newConfig.getAnnualAllowance())) {
            updateStudentBalancesForAllowanceChange(newConfig.getId(), 
                                                   oldConfig.getAnnualAllowance(), 
                                                   newConfig.getAnnualAllowance());
        }
        
        // 2. 如果请假类型被禁用，可以考虑是否需要处理正在进行的申请
        // 这里暂不处理，因为已经提交的申请应该继续按原有流程处理
    }
    
    /**
     * 更新所有学生的请假余额（当年度额度发生变化时）
     */
    @Transactional
    private void updateStudentBalancesForAllowanceChange(Integer leaveTypeId, 
                                                        Integer oldAllowance, 
                                                        Integer newAllowance) {
        // 获取当前年份
        Integer currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        // 获取该请假类型的所有学生余额记录
        List<StudentLeaveBalance> balances = studentLeaveBalanceRepository.findByLeaveTypeId(leaveTypeId);
        
        for (StudentLeaveBalance balance : balances) {
            // 只更新当前年份的余额
            if (currentYear.equals(balance.getYear())) {
                // 计算新的总额度
                balance.setTotalAllowance(newAllowance);
                
                // 重新计算剩余天数
                Double usedDays = balance.getUsedDays() != null ? balance.getUsedDays() : 0.0;
                balance.setRemainingDays(newAllowance - usedDays);
                
                // 确保剩余天数不能为负数
                if (balance.getRemainingDays() < 0) {
                    balance.setRemainingDays(0.0);
                }
                
                balance.setUpdatedAt(new Date());
                studentLeaveBalanceRepository.save(balance);
            }
        }
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

    @Transactional
    public LeaveTypeConfig activateLeaveType(Integer id) {
        LeaveTypeConfig existing = getLeaveTypeById(id);
        if (existing != null) {
            LeaveTypeConfig oldConfig = new LeaveTypeConfig();
            // 复制旧配置用于比较
            copyConfig(existing, oldConfig);
            
            existing.setEnabled(true);
            existing.setUpdatedAt(new Date());
            LeaveTypeConfig saved = leaveTypeConfigRepository.save(existing);
            
            // 激活时可能需要为新学生初始化余额
            initializeBalancesForNewlyActivatedType(saved);
            
            return saved;
        }
        return null;
    }

    @Transactional
    public LeaveTypeConfig deactivateLeaveType(Integer id) {
        LeaveTypeConfig existing = getLeaveTypeById(id);
        if (existing != null) {
            existing.setEnabled(false);
            existing.setUpdatedAt(new Date());
            return leaveTypeConfigRepository.save(existing);
            // 停用时通常不需要修改已有数据，保持历史记录完整性
        }
        return null;
    }
    
    /**
     * 为新激活的请假类型初始化学生余额
     */
    @Transactional
    private void initializeBalancesForNewlyActivatedType(LeaveTypeConfig config) {
        // 注释：这里可以根据业务需求决定是否需要为所有学生立即初始化余额
        // 当前实现选择在学生首次使用该请假类型时再初始化余额记录
        // 如果需要立即初始化，可以调用StudentService获取所有活跃学生，然后批量创建余额记录
    }
    
    /**
     * 复制配置对象
     */
    private void copyConfig(LeaveTypeConfig source, LeaveTypeConfig target) {
        target.setId(source.getId());
        target.setTypeName(source.getTypeName());
        target.setTypeCode(source.getTypeCode());
        target.setDescription(source.getDescription());
        target.setAnnualAllowance(source.getAnnualAllowance());
        target.setMaxDaysPerRequest(source.getMaxDaysPerRequest());
        target.setRequiresApproval(source.getRequiresApproval());
        target.setRequiresMedicalProof(source.getRequiresMedicalProof());
        target.setAdvanceDaysRequired(source.getAdvanceDaysRequired());
        target.setColor(source.getColor());
        target.setEnabled(source.getEnabled());
        target.setCreatedAt(source.getCreatedAt());
        target.setUpdatedAt(source.getUpdatedAt());
    }
}
