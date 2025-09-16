package com.altair288.class_management.MessageCenter.controller;

import com.altair288.class_management.MessageCenter.model.NotificationTemplate;
import com.altair288.class_management.MessageCenter.repository.NotificationTemplateRepository;
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

    public SimpleTemplateController(NotificationTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
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
}
