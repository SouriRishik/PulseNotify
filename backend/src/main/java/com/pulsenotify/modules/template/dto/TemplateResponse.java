package com.pulsenotify.modules.template.dto;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class TemplateResponse {
    private UUID id;
    private String name;
    private String subject;
    private String body;
    private UUID authorId;
    private Boolean isActive;
    private Integer version;
    private Instant createdAt;
    private Instant updatedAt;
}
