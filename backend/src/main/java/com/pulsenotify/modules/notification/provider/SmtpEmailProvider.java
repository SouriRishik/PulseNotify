package com.pulsenotify.modules.notification.provider;

import com.pulsenotify.modules.notification.entity.Notification;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SmtpEmailProvider implements NotificationProvider {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public boolean supports(String channelName) {
        return "EMAIL".equalsIgnoreCase(channelName);
    }

    @Override
    public String send(Notification notification, String compiledSubject, String compiledBody) {
        log.info("Sending REAL EMAIL to {} with subject '{}'", notification.getRecipientAddress(), compiledSubject);
        
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(notification.getRecipientAddress());
            helper.setSubject(compiledSubject != null ? compiledSubject : "Notification from PulseNotify");
            helper.setText(compiledBody, false); // false = text/plain. Set true if html is needed
            
            javaMailSender.send(message);
            
            String messageId = "SMTP_" + UUID.randomUUID().toString();
            log.info("Successfully sent email to {}. Message ID: {}", notification.getRecipientAddress(), messageId);
            return messageId;
            
        } catch (MessagingException e) {
            log.error("Failed to construct or send email to {}", notification.getRecipientAddress(), e);
            throw new RuntimeException("Failed to send email via SMTP", e);
        }
    }
}
