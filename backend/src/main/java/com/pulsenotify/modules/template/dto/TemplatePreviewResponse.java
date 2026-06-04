package com.pulsenotify.modules.template.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TemplatePreviewResponse {
    private String compiledSubject;
    private String compiledBody;
}
