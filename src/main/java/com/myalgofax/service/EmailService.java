package com.myalgofax.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EmailService {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public Mono<Void> sendWelcomeEmail(String toEmail, String userName, String userId) {
        if (toEmail == null || userName == null || userId == null) {
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
            String subject = "Welcome to MyAlgoFax!";
            String body = "Hi " + userName + ",\n\n"
                    + "Thanks for registering with MyAlgoFax!\n\n"
                    + "🆔 Your User ID: " + userId + "\n"
                    + "🔐 You can log in using the link below:\n"
                    + "👉 http://localhost:3000/login\n\n"
                    + "Best regards,\n"
                    + "The MyAlgoFax Team";

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        })
        .subscribeOn(Schedulers.boundedElastic())
        .then()
        .onErrorResume(e -> {
            logger.error("Failed to send welcome email", e);
            return Mono.empty();
        });
    }
}
