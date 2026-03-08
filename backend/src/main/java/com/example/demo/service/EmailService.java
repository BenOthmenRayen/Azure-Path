package com.example.demo.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Email sending service.
 * - sendSimpleMessage(...) : simple plain-text email (keeps backwards compatibility)
 * - sendOtpEmail(...)      : specialized method used by OTP controller (plain + HTML)
 *
 * Make sure you have configured spring.mail.* in application.properties and env vars.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.from:no-reply@azurepath.example.com}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * Simple plain-text email (existing method).
     */
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(fromAddress);
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(text);
        mailSender.send(msg);
        log.debug("Simple email sent to {}", to);
    }

    /**
     * Send OTP email. Uses both plain text and HTML fallback.
     *
     * @param to         recipient email
     * @param otp        the 6-digit OTP string
     * @param ttlMinutes lifetime in minutes to mention in the message
     */
    public void sendOtpEmail(String to, String otp, int ttlMinutes) {
        String subject = "Votre code de vérification AzurePath";
        String plain = "Bonjour,\n\n"
                + "Votre code de vérification AzurePath est : " + otp + "\n\n"
                + "Ce code expire dans " + ttlMinutes + " minutes. Ne le partagez pas.\n\n"
                + "— L'équipe AzurePath";

        String html = "<div style='font-family:Arial,sans-serif;color:#222;'>"
                + "<p>Bonjour,</p>"
                + "<p>Votre code de vérification AzurePath est :</p>"
                + "<h1 style='letter-spacing:6px;font-size:36px;margin:10px 0;'>" + otp + "</h1>"
                + "<p>Ce code expire dans <strong>" + ttlMinutes + " minutes</strong>. Ne le partagez pas.</p>"
                + "<hr/><p>— L'équipe AzurePath</p></div>";

        try {
            // prefer HTML message composed with fallback text
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(plain, html); // plain + html
            mailSender.send(msg);
            log.info("OTP email sent to {} (ttl={}min)", to, ttlMinutes);
        } catch (Exception e) {
            // fallback to plain text if something goes wrong with MimeMessage
            log.warn("Failed to send HTML OTP email to {}, falling back to plain: {}", to, e.getMessage());
            sendSimpleMessage(to, subject, plain);
        }
    }
}
