package com.altair288.class_management.MessageCenter.support;

import java.util.*;

/**
 * 定义请假相关通知支持的变量元数据。
 * 不做数据库配置，保持轻量；未来可迁移到表结构。
 */
public final class LeaveNotificationVariableCatalog {

    public record VariableMeta(String name, String label, String group, String type, String example, boolean required) {}

    private static final List<VariableMeta> ALL = List.of(
            new VariableMeta("leaveId","申请ID","流程上下文","number","123", true),
            new VariableMeta("studentName","学生姓名","申请人信息","string","张三", true),
            new VariableMeta("studentNo","学号","申请人信息","string","20240001", true),
            new VariableMeta("className","班级","班级/院系","string","计科23-1", false),
            new VariableMeta("departmentName","系部","班级/院系","string","计算机系", false),
            new VariableMeta("leaveTypeName","请假类型","请假信息","string","病假", true),
            new VariableMeta("startDate","开始日期","请假信息","date","2025-09-17", true),
            new VariableMeta("endDate","结束日期","请假信息","date","2025-09-19", true),
            new VariableMeta("days","天数","请假信息","number","2", true),
            new VariableMeta("currentStepName","当前步骤","流程上下文","string","系主任审批", false),
            new VariableMeta("rejectReason","拒绝原因","流程上下文","string","材料不全", false)
    );

    public static List<VariableMeta> listAll() { return ALL; }

    public static Map<String,Object> groupBy() {
        Map<String,List<Map<String,Object>>> grouped = new LinkedHashMap<>();
    for (VariableMeta m : ALL) {
        List<Map<String,Object>> arr = grouped.get(m.group());
        if (arr == null) { arr = new ArrayList<>(); grouped.put(m.group(), arr); }
        arr.add(Map.of(
            "name", m.name(),
            "label", m.label(),
            "type", m.type(),
            "example", m.example(),
            "required", m.required()
        ));
    }
        // 保持插入顺序
        Map<String,Object> result = new LinkedHashMap<>();
        List<Map<String,Object>> groupArr = new ArrayList<>();
        for (var e : grouped.entrySet()) {
            groupArr.add(Map.of("group", e.getKey(), "items", e.getValue()));
        }
        result.put("groups", groupArr);
        return result;
    }
}
