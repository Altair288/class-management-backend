package com.altair288.class_management.service;

import com.altair288.class_management.model.LeaveTypeConfig;
import com.altair288.class_management.model.StudentLeaveBalance;
import com.altair288.class_management.repository.LeaveTypeConfigRepository;
import com.altair288.class_management.repository.LeaveRequestRepository;
import com.altair288.class_management.repository.StudentLeaveBalanceRepository;
import com.altair288.class_management.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentLeaveBalanceService studentLeaveBalanceService;

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
        } else {
            // 新增类型：若启用，则为所有学生初始化当年余额
            if (Boolean.TRUE.equals(savedConfig.getEnabled())) {
                initializeBalancesForNewlyActivatedType(savedConfig);
            }
        }
        
        return savedConfig;
    }
    
    /**
     * 配置更新后同步更新相关数据
     */
    @Transactional
    private void updateRelatedDataAfterConfigChange(LeaveTypeConfig oldConfig, LeaveTypeConfig newConfig) {
        System.out.println("检查配置变更 - 旧额度: " + oldConfig.getAnnualAllowance() + ", 新额度: " + newConfig.getAnnualAllowance());
        
        // 1. 如果年度额度发生变化，更新所有学生的年度余额
        if (!oldConfig.getAnnualAllowance().equals(newConfig.getAnnualAllowance())) {
            System.out.println("年度额度发生变化，开始更新学生余额...");
            // 优先使用批量 SQL 更新，确保所有记录（含历史年度）被同步
            try {
                int total = studentLeaveBalanceRepository.bulkUpdateAllYearsByType(newConfig.getId(), newConfig.getAnnualAllowance());
                System.out.println("批量更新完成，影响记录数: " + total);
            } catch (Exception ex) {
                // 回退到逐条更新逻辑
                System.out.println("批量更新失败，回退到逐条更新: " + ex.getMessage());
                updateStudentBalancesForAllowanceChange(newConfig.getId(), 
                                                       oldConfig.getAnnualAllowance(), 
                                                       newConfig.getAnnualAllowance());
            }
            System.out.println("学生余额更新完成");
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
        // 获取该请假类型的所有学生余额记录
        List<StudentLeaveBalance> balances = studentLeaveBalanceRepository.findByLeaveTypeId(leaveTypeId);
        System.out.println("找到 " + balances.size() + " 条学生余额记录需要更新");
        
        for (StudentLeaveBalance balance : balances) {
            System.out.println("更新学生ID: " + balance.getStudentId() + 
                             ", 年份: " + balance.getYear() + 
                             ", 旧额度: " + balance.getTotalAllowance() + 
                             " -> 新额度: " + newAllowance);
            
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
            System.out.println("余额记录已更新: 剩余天数 = " + balance.getRemainingDays());
        }
    }

    public void deleteLeaveType(Integer id) {
        // 如存在关联数据，不再硬删除，改为逻辑禁用
        Long leaveRequestCount = leaveRequestRepository.countByLeaveTypeId(id);
        Long balanceCount = studentLeaveBalanceRepository.countByLeaveTypeId(id);
        if ((leaveRequestCount != null && leaveRequestCount > 0) || (balanceCount != null && balanceCount > 0)) {
            deactivateLeaveType(id);
            return;
        }
        // 无关联数据，允许物理删除（兼容早期无数据的配置）
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
        try {
            Integer year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            Integer allowance = config.getAnnualAllowance() == null ? 0 : config.getAnnualAllowance();
            var allStudents = studentRepository.findAll();
            if (allStudents == null || allStudents.isEmpty()) return;
            java.util.List<Integer> ids = new java.util.ArrayList<>(allStudents.size());
            for (var s : allStudents) { ids.add(s.getId()); }
            studentLeaveBalanceService.batchInitializeBalances(ids, config.getId(), year, allowance);
        } catch (Exception ignored) {}
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

    /**
     * 手动触发余额同步（当历史数据与配置不一致时）
     */
    @Transactional
    public int syncBalancesForLeaveType(Integer leaveTypeId, boolean onlyCurrentYear) {
        LeaveTypeConfig cfg = getLeaveTypeById(leaveTypeId);
        if (cfg == null) throw new IllegalArgumentException("请假类型不存在");
        int updated;
        if (onlyCurrentYear) {
            int year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            updated = studentLeaveBalanceRepository.bulkUpdateByTypeAndYear(leaveTypeId, year, cfg.getAnnualAllowance());
        } else {
            updated = studentLeaveBalanceRepository.bulkUpdateAllYearsByType(leaveTypeId, cfg.getAnnualAllowance());
        }
        return updated;
    }
}
