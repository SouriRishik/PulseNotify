package com.pulsenotify.modules.notification.repository;

import com.pulsenotify.modules.notification.entity.DeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

import com.pulsenotify.modules.auth.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface DeliveryEventRepository extends JpaRepository<DeliveryEvent, UUID> {
    
    @Query("SELECT COUNT(d) FROM DeliveryEvent d WHERE d.notification.user = :user AND d.eventType = :eventType")
    long countByUserAndEventType(@Param("user") User user, @Param("eventType") String eventType);
}
