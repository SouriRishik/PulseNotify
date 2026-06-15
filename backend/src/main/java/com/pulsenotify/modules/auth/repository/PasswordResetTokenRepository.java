package com.pulsenotify.modules.auth.repository;

import com.pulsenotify.modules.auth.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    Optional<PasswordResetToken> findByTokenAndUserEmail(String token, String email);
    List<PasswordResetToken> findByUserEmail(String email);
}
