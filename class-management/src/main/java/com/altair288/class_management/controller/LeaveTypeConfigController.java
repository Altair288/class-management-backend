package com.altair288.class_management.controller;

import com.altair288.class_management.model.LeaveTypeConfig;
import com.altair288.class_management.service.LeaveTypeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/leave/config")
@CrossOrigin(origins = "http://localhost:3000")
public class LeaveTypeConfigController {
    
    @Autowired
    private LeaveTypeConfigService leaveTypeConfigService;

    // 获取所有激活的请假类型 用户申请时查询接口
    @GetMapping("/active")
    public ResponseEntity<List<LeaveTypeConfig>> getActiveLeaveTypes() {
        List<LeaveTypeConfig> types = leaveTypeConfigService.getAllActiveLeaveTypes();
        return ResponseEntity.ok(types);
    }

    // 获取所有请假类型（包括已停用的）系统配置时查询接口
    @GetMapping("/all")
    public ResponseEntity<List<LeaveTypeConfig>> getAllLeaveTypes() {
        List<LeaveTypeConfig> types = leaveTypeConfigService.getAllLeaveTypes();
        return ResponseEntity.ok(types);
    }

    // 获取单个请假类型
    @GetMapping("/{id}")
    public ResponseEntity<LeaveTypeConfig> getLeaveTypeById(@PathVariable Integer id) {
        LeaveTypeConfig type = leaveTypeConfigService.getLeaveTypeById(id);
        if (type != null) {
            return ResponseEntity.ok(type);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 根据名称获取请假类型
    @GetMapping("/name/{typeName}")
    public ResponseEntity<LeaveTypeConfig> getLeaveTypeByName(@PathVariable String typeName) {
        LeaveTypeConfig type = leaveTypeConfigService.getLeaveTypeByName(typeName);
        if (type != null) {
            return ResponseEntity.ok(type);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 创建新的请假类型
    @PostMapping
    public ResponseEntity<LeaveTypeConfig> createLeaveType(@RequestBody LeaveTypeConfig leaveTypeConfig) {
        try {
            LeaveTypeConfig saved = leaveTypeConfigService.saveLeaveType(leaveTypeConfig);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 更新请假类型
    @PutMapping("/{id}")
    public ResponseEntity<LeaveTypeConfig> updateLeaveType(
            @PathVariable Integer id, 
            @RequestBody LeaveTypeConfig leaveTypeConfig) {
        LeaveTypeConfig existing = leaveTypeConfigService.getLeaveTypeById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        leaveTypeConfig.setId(id);
        LeaveTypeConfig updated = leaveTypeConfigService.saveLeaveType(leaveTypeConfig);
        return ResponseEntity.ok(updated);
    }

    // 激活请假类型
    @PostMapping("/{id}/activate")
    public ResponseEntity<LeaveTypeConfig> activateLeaveType(@PathVariable Integer id) {
        LeaveTypeConfig activated = leaveTypeConfigService.activateLeaveType(id);
        if (activated != null) {
            return ResponseEntity.ok(activated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 停用请假类型
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<LeaveTypeConfig> deactivateLeaveType(@PathVariable Integer id) {
        LeaveTypeConfig deactivated = leaveTypeConfigService.deactivateLeaveType(id);
        if (deactivated != null) {
            return ResponseEntity.ok(deactivated);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 手动触发该类型余额同步（onlyCurrentYear=true 只同步当前学年）
    @PostMapping("/{id}/sync-balances")
    public ResponseEntity<java.util.Map<String,Object>> syncBalances(@PathVariable Integer id,
                                                                     @RequestParam(defaultValue = "true") boolean onlyCurrentYear) {
        int updated = leaveTypeConfigService.syncBalancesForLeaveType(id, onlyCurrentYear);
        java.util.Map<String,Object> resp = new java.util.HashMap<>();
        resp.put("updated", updated);
        resp.put("onlyCurrentYear", onlyCurrentYear);
        return ResponseEntity.ok(resp);
    }

    // 删除请假类型：若存在关联数据则改为逻辑禁用
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveType(@PathVariable Integer id) {
        LeaveTypeConfig existing = leaveTypeConfigService.getLeaveTypeById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            leaveTypeConfigService.deleteLeaveType(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            // 理论上不会抛出，因为服务层已转为逻辑禁用，这里兜底返回 200
            return ResponseEntity.ok().build();
        }
    }
}
