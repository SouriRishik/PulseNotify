package com.pulsenotify.modules.template.controller;

import com.pulsenotify.modules.auth.dto.MessageResponse;
import com.pulsenotify.modules.template.dto.*;
import com.pulsenotify.modules.template.service.TemplateService;
import com.pulsenotify.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @Valid @RequestBody TemplateCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TemplateResponse response = templateService.createTemplate(request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getTemplateById(@PathVariable UUID id) {
        return ResponseEntity.ok(templateService.getTemplateById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody TemplateUpdateRequest request) {
        return ResponseEntity.ok(templateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTemplate(@PathVariable UUID id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.ok(new MessageResponse("Template deleted successfully"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TemplateResponse> toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean active) {
        return ResponseEntity.ok(templateService.toggleTemplateStatus(id, active));
    }

    @PostMapping("/{id}/preview")
    public ResponseEntity<TemplatePreviewResponse> previewTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody TemplatePreviewRequest request) {
        return ResponseEntity.ok(templateService.previewTemplate(id, request));
    }
}
