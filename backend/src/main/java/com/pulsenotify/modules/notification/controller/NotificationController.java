package com.pulsenotify.modules.notification.controller;

import com.pulsenotify.modules.notification.dto.NotificationSubmissionResponse;
import com.pulsenotify.modules.notification.dto.SendNotificationRequest;
import com.pulsenotify.modules.notification.service.NotificationService;
import com.pulsenotify.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationSubmissionResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        NotificationSubmissionResponse response = notificationService.submitNotification(request, userDetails.getId());
        return ResponseEntity.accepted().body(response);
    }
}
