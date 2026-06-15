package com.pulsenotify.modules.auth.service;

import com.pulsenotify.modules.auth.dto.LoginRequest;
import com.pulsenotify.modules.auth.dto.SignupRequest;
import com.pulsenotify.modules.auth.dto.JwtResponse;
import com.pulsenotify.modules.auth.dto.MessageResponse;
import com.pulsenotify.modules.auth.entity.ERole;
import com.pulsenotify.modules.auth.entity.RefreshToken;
import com.pulsenotify.modules.auth.entity.Role;
import com.pulsenotify.modules.auth.entity.User;
import com.pulsenotify.modules.auth.entity.PendingUser;
import com.pulsenotify.modules.auth.repository.RoleRepository;
import com.pulsenotify.modules.auth.repository.UserRepository;
import com.pulsenotify.modules.auth.repository.PendingUserRepository;
import com.pulsenotify.modules.auth.entity.PasswordResetToken;
import com.pulsenotify.modules.auth.repository.PasswordResetTokenRepository;
import com.pulsenotify.security.jwt.JwtUtils;
import com.pulsenotify.security.services.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;
    private final PendingUserRepository pendingUserRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return JwtResponse.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .id(userDetails.getId())
                .email(userDetails.getEmail())
                .firstName(userDetails.getFirstName())
                .lastName(userDetails.getLastName())
                .roles(roles)
                .build();
    }

    public MessageResponse registerUser(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Error: Email is already in use!");
        }

        // Generate 6-digit verification OTP
        Random random = new Random();
        int otpCode = 100000 + random.nextInt(900000);
        String tokenString = String.valueOf(otpCode);

        String rolesString = "";
        Set<String> strRoles = signUpRequest.getRoles();
        if (strRoles != null && !strRoles.isEmpty()) {
            rolesString = String.join(",", strRoles);
        } else {
            rolesString = "user";
        }

        Optional<PendingUser> existingPending = pendingUserRepository.findByEmail(signUpRequest.getEmail());
        PendingUser pendingUser;
        if (existingPending.isPresent()) {
            pendingUser = existingPending.get();
            pendingUser.setFirstName(signUpRequest.getFirstName());
            pendingUser.setLastName(signUpRequest.getLastName());
            pendingUser.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
            pendingUser.setRoles(rolesString);
            pendingUser.setOtpCode(tokenString);
            pendingUser.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        } else {
            pendingUser = PendingUser.builder()
                    .email(signUpRequest.getEmail())
                    .passwordHash(encoder.encode(signUpRequest.getPassword()))
                    .firstName(signUpRequest.getFirstName())
                    .lastName(signUpRequest.getLastName())
                    .roles(rolesString)
                    .otpCode(tokenString)
                    .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                    .build();
        }

        pendingUserRepository.save(pendingUser);
        
        // Send email
        emailService.sendVerificationEmail(pendingUser.getEmail(), tokenString);

        return new MessageResponse("User registered successfully! Please check your email to verify your account.");
    }

    public MessageResponse verifyEmail(String email, String token) {
        PendingUser pendingUser = pendingUserRepository.findByEmailAndOtpCode(email, token)
                .orElseThrow(() -> new RuntimeException("Error: Invalid verification token or email."));

        if (pendingUser.getExpiresAt().isBefore(Instant.now())) {
            pendingUserRepository.delete(pendingUser);
            throw new RuntimeException("Error: Verification token has expired. Please request a new one.");
        }

        // Move to real User table
        User user = User.builder()
                .email(pendingUser.getEmail())
                .passwordHash(pendingUser.getPasswordHash())
                .firstName(pendingUser.getFirstName())
                .lastName(pendingUser.getLastName())
                .build();

        Set<Role> roles = new HashSet<>();
        if (pendingUser.getRoles() == null || pendingUser.getRoles().isEmpty() || pendingUser.getRoles().equals("user")) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            for (String roleStr : pendingUser.getRoles().split(",")) {
                if (roleStr.equals("admin")) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                } else {
                    Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(userRole);
                }
            }
        }
        user.setRoles(roles);
        userRepository.save(user);

        // Delete pending user
        pendingUserRepository.delete(pendingUser);

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail());

        return new MessageResponse("Email verified successfully!");
    }

    public MessageResponse resendOtp(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Error: Email is already verified.");
        }

        PendingUser pendingUser = pendingUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: User not found with email: " + email + ". Please register first."));

        // Generate new 6-digit OTP
        Random random = new Random();
        int otpCode = 100000 + random.nextInt(900000);
        String tokenString = String.valueOf(otpCode);

        pendingUser.setOtpCode(tokenString);
        pendingUser.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        pendingUserRepository.save(pendingUser);

        // Send email
        emailService.sendVerificationEmail(pendingUser.getEmail(), tokenString);

        return new MessageResponse("A new OTP has been sent to your email.");
    }

    public MessageResponse requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Error: User not found with email: " + email));

        // Generate 6-digit OTP
        Random random = new Random();
        int otpCode = 100000 + random.nextInt(900000);
        String tokenString = String.valueOf(otpCode);

        // Delete existing tokens for this user
        passwordResetTokenRepository.deleteAll(passwordResetTokenRepository.findByUserEmail(email));

        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenString)
                .user(user)
                .expiresAt(Instant.now().plus(15, ChronoUnit.MINUTES))
                .build();

        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetEmail(user.getEmail(), tokenString);

        return new MessageResponse("Password reset OTP sent to your email.");
    }

    public MessageResponse resetPassword(String email, String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenAndUserEmail(token, email)
                .orElseThrow(() -> new RuntimeException("Error: Invalid password reset token or email."));

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new RuntimeException("Error: Password reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(encoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        return new MessageResponse("Password successfully reset!");
    }
}
