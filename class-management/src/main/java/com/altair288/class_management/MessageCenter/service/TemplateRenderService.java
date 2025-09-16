package com.altair288.class_management.MessageCenter.service;

import com.altair288.class_management.MessageCenter.model.NotificationTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateRenderService {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{([a-zA-Z0-9_]+)\\}");

    public record RenderResult(String title, String content) {}

    public RenderResult render(NotificationTemplate template, Map<String, Object> vars) {
        if (template == null) return new RenderResult("", "");
        String title = replace(template.getTitleTemplate(), vars);
        String content = replace(template.getContentTemplate(), vars);
        return new RenderResult(title, content);
    }

    private String replace(String tpl, Map<String, Object> vars) {
        if (tpl == null) return "";
        Matcher m = VAR_PATTERN.matcher(tpl);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String key = m.group(1);
            Object v = vars.get(key);
            String rep = v == null? "" : String.valueOf(v);
            rep = Matcher.quoteReplacement(rep);
            m.appendReplacement(sb, rep);
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
