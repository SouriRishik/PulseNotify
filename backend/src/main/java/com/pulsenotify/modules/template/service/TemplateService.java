package com.pulsenotify.modules.template.service;

import com.pulsenotify.modules.auth.entity.User;
import com.pulsenotify.modules.auth.repository.UserRepository;
import com.pulsenotify.modules.template.dto.*;
import com.pulsenotify.modules.template.entity.NotificationTemplate;
import com.pulsenotify.modules.template.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final UserRepository userRepository;

    @Transactional
    public TemplateResponse createTemplate(TemplateCreateRequest request, UUID authorId) {
        if (templateRepository.existsByName(request.getName())) {
            throw new RuntimeException("Template name already exists");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Author not found"));

        NotificationTemplate template = NotificationTemplate.builder()
                .name(request.getName())
                .subject(request.getSubject())
                .body(request.getBody())
                .author(author)
                .isActive(true)
                .build();

        return mapToResponse(templateRepository.save(template));
    }

    public List<TemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TemplateResponse getTemplateById(UUID id) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        return mapToResponse(template);
    }

    @Transactional
    public TemplateResponse updateTemplate(UUID id, TemplateUpdateRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        if (request.getIsActive() != null) {
            template.setIsActive(request.getIsActive());
        }

        return mapToResponse(templateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException("Template not found");
        }
        templateRepository.deleteById(id);
    }

    @Transactional
    public TemplateResponse toggleTemplateStatus(UUID id, boolean isActive) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));
        template.setIsActive(isActive);
        return mapToResponse(templateRepository.save(template));
    }

    public TemplatePreviewResponse previewTemplate(UUID id, TemplatePreviewRequest request) {
        NotificationTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found"));

        String compiledSubject = compile(template.getSubject(), request.getPayload());
        String compiledBody = compile(template.getBody(), request.getPayload());

        return new TemplatePreviewResponse(compiledSubject, compiledBody);
    }

    private String compile(String text, Map<String, String> payload) {
        if (text == null) return null;
        String compiled = text;
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            compiled = compiled.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return compiled;
    }

    private TemplateResponse mapToResponse(NotificationTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .subject(template.getSubject())
                .body(template.getBody())
                .authorId(template.getAuthor() != null ? template.getAuthor().getId() : null)
                .isActive(template.getIsActive())
                .version(template.getVersion())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
