package com.pulsenotify.modules.auth.repository;

import com.pulsenotify.modules.auth.entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, UUID> {
    Optional<PendingUser> findByEmail(String email);
    Optional<PendingUser> findByEmailAndOtpCode(String email, String otpCode);
}
