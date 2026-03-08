package com.example.demo.service;


import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class HtmlEmailService {
    private final JavaMailSender mailSender;
    private final String from;
    private final Logger log = LoggerFactory.getLogger(HtmlEmailService.class);


    public HtmlEmailService(JavaMailSender mailSender, @Value("${app.mail.from:${spring.mail.username}}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }


    public void sendHtml(String to, String subject, String htmlBody, String plainTextFallback) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plainTextFallback, htmlBody);
            mailSender.send(msg);
            log.info("Email envoyé à {} (subject={})", to, subject);
        } catch (Exception e) {
            log.error("Erreur envoi mail à {} : {}", to, e.getMessage(), e);
            throw new RuntimeException("Mail send failed", e);
        }
    }
}