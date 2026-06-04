package com.pulsenotify.modules.template.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.Map;

@Data
public class TemplatePreviewRequest {
    @NotEmpty
    private Map<String, String> payload;
}
