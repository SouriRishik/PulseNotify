package com.pulsenotify.modules.notification.repository;

import com.pulsenotify.modules.notification.entity.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationChannelRepository extends JpaRepository<NotificationChannel, Integer> {
    Optional<NotificationChannel> findByChannelName(String channelName);
}
