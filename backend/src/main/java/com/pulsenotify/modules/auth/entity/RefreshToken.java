package com.pulsenotify.modules.auth.entity;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    private User user;
    private String token;
    private Instant expiresAt;
    
    @Builder.Default
    private Boolean isRevoked = false;
}
