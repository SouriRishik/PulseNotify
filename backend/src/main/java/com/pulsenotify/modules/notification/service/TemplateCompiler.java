package com.pulsenotify.modules.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class TemplateCompiler {

    private final ObjectMapper objectMapper;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public TemplateCompiler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Compiles a template string by replacing {{variable}} with the corresponding value from the JSON payload.
     *
     * @param template    The raw template string containing {{variable}} placeholders.
     * @param jsonPayload The JSON string representing the data variables.
     * @return The compiled string.
     */
    public String compile(String template, Map<String, Object> variables) {
        if (template == null || template.trim().isEmpty()) {
            return template;
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        try {
            Matcher matcher = VARIABLE_PATTERN.matcher(template);
            StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                String variableName = matcher.group(1).trim();
                Object value = variables.get(variableName);
                
                String replacement = value != null ? value.toString() : matcher.group(0); // If not found, keep the original placeholder
                
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            
            return sb.toString();

        } catch (Exception e) {
            log.error("Error occurred while compiling template.", e);
            return template;
        }
    }
}
