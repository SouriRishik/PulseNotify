package com.pulsenotify.modules.template.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TemplateUpdateRequest {
    @NotBlank
    private String subject;
    
    @NotBlank
    private String body;
    
    private Boolean isActive;
}
