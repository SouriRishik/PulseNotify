package com.pulsenotify.modules.notification.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notification_channels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationChannel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "channel_name", nullable = false, unique = true, length = 50)
    private String channelName;
}
