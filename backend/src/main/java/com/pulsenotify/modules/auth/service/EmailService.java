package com.pulsenotify.modules.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            // Frontend verification URL is no longer needed, we send the OTP directly
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@pulsenotify.com");
            message.setTo(toEmail);
            message.setSubject("Verify your PulseNotify account");
            message.setText("Welcome to PulseNotify! \n\n" +
                    "Your 6-digit verification code is:\n\n" +
                    token + "\n\n" +
                    "Please enter this code on the verification page. This code will expire in 24 hours.");
            
            mailSender.send(message);
            log.info("Sent verification email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", toEmail, e);
        }
    }

    public void sendWelcomeEmail(String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@pulsenotify.com");
            message.setTo(toEmail);
            message.setSubject("Welcome to PulseNotify!");
            message.setText("Congratulations! \n\n" +
                    "Your email has been successfully verified and your account is fully set up.\n\n" +
                    "Welcome aboard, we're thrilled to have you at PulseNotify!");
            
            mailSender.send(message);
            log.info("Sent welcome email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}", toEmail, e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@pulsenotify.com");
            message.setTo(toEmail);
            message.setSubject("Reset your PulseNotify Password");
            message.setText("We received a request to reset your password. \n\n" +
                    "Your 6-digit password reset code is:\n\n" +
                    token + "\n\n" +
                    "Please enter this code on the reset password page. This code will expire in 15 minutes.\n" +
                    "If you did not request a password reset, please ignore this email.");
            
            mailSender.send(message);
            log.info("Sent password reset email to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}", toEmail, e);
        }
    }
}
