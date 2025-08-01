package com.algo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String userName, String userId) {
        if (toEmail == null || userName == null || userId == null) return;

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
    }
}
