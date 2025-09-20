package com.altair288.class_management.MessageCenter.controller;

import com.altair288.class_management.MessageCenter.model.NotificationTemplate;
import com.altair288.class_management.MessageCenter.repository.NotificationTemplateRepository;
import com.altair288.class_management.MessageCenter.support.LeaveNotificationVariableCatalog;
import com.altair288.class_management.MessageCenter.service.TemplateRenderService;
import com.altair288.class_management.service.LeaveRequestService;
import com.altair288.class_management.model.LeaveRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 简化模板配置接口：不做版本管理，只编辑当前 active 模板。
 */
@RestController
@RequestMapping("/api/notification-templates/simple")
public class SimpleTemplateController {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateRenderService templateRenderService;
    private final LeaveRequestService leaveRequestService;

    public SimpleTemplateController(NotificationTemplateRepository templateRepository,
                                    TemplateRenderService templateRenderService,
                                    LeaveRequestService leaveRequestService) {
        this.templateRepository = templateRepository;
        this.templateRenderService = templateRenderService;
        this.leaveRequestService = leaveRequestService;
    }

    public record UpsertRequest(String titleTemplate, String contentTemplate, String remark) {}

    @GetMapping("/{code}")
    public Map<String,Object> getByCode(@PathVariable String code) {
        var tpl = templateRepository.findLatestActiveByCode(code).orElse(null);
        if (tpl == null) return Map.of("code", code, "exists", false);
        return Map.of(
                "code", tpl.getCode(),
                "titleTemplate", tpl.getTitleTemplate(),
                "contentTemplate", tpl.getContentTemplate(),
                "remark", tpl.getRemark(),
                "version", tpl.getVersion(),
                "status", tpl.getStatus()
        );
    }

    @Transactional
    @PostMapping("/{code}")
    public Map<String,Object> upsert(@PathVariable String code, @RequestBody UpsertRequest req) {
        // 查找当前 active
        NotificationTemplate tpl = templateRepository.findLatestActiveByCode(code).orElse(null);
        if (tpl == null) {
            tpl = new NotificationTemplate();
            tpl.setCode(code);
            tpl.setStatus("ACTIVE");
            tpl.setVersion(1); // 固定 1，不做版本迭代
        }
        if (req.titleTemplate() != null) tpl.setTitleTemplate(req.titleTemplate());
        if (req.contentTemplate() != null) tpl.setContentTemplate(req.contentTemplate());
        if (req.remark() != null) tpl.setRemark(req.remark());
        templateRepository.save(tpl);
        return Map.of("code", tpl.getCode(), "saved", true);
    }

    // 变量元数据（当前只返回请假领域统一列表）
    @GetMapping("/variables")
    public Map<String,Object> variables() {
        return LeaveNotificationVariableCatalog.groupBy();
    }

    public record PreviewRequest(
            String templateCode,
            String titleTemplate,
            String contentTemplate,
            Long leaveRequestId,
            Map<String,Object> variables,
            Map<String,Object> overrideVariables
    ) {}

    @PostMapping("/preview")
    public Map<String,Object> preview(@RequestBody PreviewRequest req) {
        Map<String,Object> base = new java.util.HashMap<>();
        // 如果提供 leaveRequestId，构造基础变量（重用 LeaveRequestService 内已有逻辑：反射/复制）
        if (req.leaveRequestId() != null) {
            LeaveRequest lr = leaveRequestService.getById(req.leaveRequestId().intValue());
            if (lr != null) {
                // 复用其内部变量构造：为避免循环依赖，这里简单复制逻辑（或可抽取公共 Bean）
                base.put("leaveId", lr.getId());
                if (lr.getStudent() != null) {
                    base.put("studentName", safe(lr.getStudent().getName()));
                    base.put("studentNo", safe(lr.getStudent().getStudentNo()));
                    if (lr.getStudent().getClazz() != null) {
                        base.put("className", safe(lr.getStudent().getClazz().getName()));
                        if (lr.getStudent().getClazz().getDepartment() != null) {
                            base.put("departmentName", safe(lr.getStudent().getClazz().getDepartment().getName()));
                        }
                    }
                }
                if (lr.getLeaveTypeConfig() != null) base.put("leaveTypeName", safe(lr.getLeaveTypeConfig().getTypeName()));
                if (lr.getStartDate() != null) base.put("startDate", formatDate(lr.getStartDate()));
                if (lr.getEndDate() != null) base.put("endDate", formatDate(lr.getEndDate()));
                if (lr.getDays() != null) base.put("days", lr.getDays());
            }
        }
        if (req.variables() != null) base.putAll(req.variables());
        if (req.overrideVariables() != null) base.putAll(req.overrideVariables());

        String titleTpl = req.titleTemplate();
        String contentTpl = req.contentTemplate();
        if ((titleTpl == null || contentTpl == null) && req.templateCode() != null) {
            var tpl = templateRepository.findLatestActiveByCode(req.templateCode()).orElse(null);
            if (tpl != null) {
                if (titleTpl == null) titleTpl = tpl.getTitleTemplate();
                if (contentTpl == null) contentTpl = tpl.getContentTemplate();
            }
        }
        var dr = templateRenderService.renderWithDiagnostics(titleTpl, contentTpl, base);
        return Map.of(
                "renderedTitle", dr.renderedTitle(),
                "renderedContent", dr.renderedContent(),
                "used", dr.used(),
                "missing", dr.missing(),
                "unused", dr.unused(),
                "rawVariables", base
        );
    }

    private String safe(String s) { return s == null? "": s; }
    private String formatDate(java.util.Date d) { return d == null? null : d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString(); }
}
