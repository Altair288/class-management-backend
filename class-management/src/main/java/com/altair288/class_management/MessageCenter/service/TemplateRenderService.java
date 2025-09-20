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

    public record DiagnosticResult(String renderedTitle, String renderedContent,
                                   java.util.Set<String> used,
                                   java.util.Set<String> missing,
                                   java.util.Set<String> unused) {}

    public DiagnosticResult renderWithDiagnostics(String titleTemplate, String contentTemplate, Map<String,Object> variables) {
        if (titleTemplate == null) titleTemplate = "";
        if (contentTemplate == null) contentTemplate = "";
        java.util.Set<String> placeholders = extractPlaceholders(titleTemplate + "\n" + contentTemplate);
        java.util.Set<String> used = new java.util.HashSet<>();
        String renderedTitle = replaceVarsCollect(titleTemplate, variables, used);
        String renderedContent = replaceVarsCollect(contentTemplate, variables, used);
        java.util.Set<String> missing = new java.util.HashSet<>();
        for (String p : placeholders) if (!variables.containsKey(p)) missing.add(p);
        java.util.Set<String> unused = new java.util.HashSet<>(variables.keySet());
        unused.removeAll(used);
        return new DiagnosticResult(renderedTitle, renderedContent, used, missing, unused);
    }

    private java.util.Set<String> extractPlaceholders(String text) {
        java.util.Set<String> set = new java.util.HashSet<>();
        java.util.regex.Matcher m = VAR_PATTERN.matcher(text);
        while (m.find()) set.add(m.group(1));
        return set;
    }

    private String replaceVarsCollect(String template, Map<String,Object> vars, java.util.Set<String> used) {
        if (template == null) return null;
        java.util.regex.Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String var = matcher.group(1);
            Object val = vars.get(var);
            if (val != null) { used.add(var); matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(String.valueOf(val))); }
            else { matcher.appendReplacement(sb, "{"+var+"}"); }
        }
        matcher.appendTail(sb);
        return sb.toString();
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
