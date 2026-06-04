package com.pulsenotify.modules.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "retry_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class RetryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 1;

    @LastModifiedDate
    @Column(name = "last_retry", nullable = false)
    private Instant lastRetry;

    @Column(name = "error_reason", columnDefinition = "TEXT")
    private String errorReason;
}
