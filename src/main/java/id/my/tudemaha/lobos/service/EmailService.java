package id.my.tudemaha.lobos.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(String to, String purpose, String firstName, String rawToken) {
        String verificationUrl = baseUrl + "/verify-email?t=" + rawToken;

        Context context = new Context();
        context.setVariable("verificationUrl", verificationUrl);

        StringBuilder subject = new StringBuilder();

        switch (purpose) {
            case "CHANGE_EMAIL":
                subject.append("Confirm your new email address for Lobos");
                context.setVariable("title", subject.toString());
                context.setVariable("heading", "Confirm your new email address");
                context.setVariable("message", "Hi " + firstName + ", we received a request" +
                    " to update your account email address. Click below to verify and complete the change.");
                context.setVariable("buttonText", "Confirm Email Change");
                break;
            case "REGISTER":
                subject.append("Verify your Lobos account");
                context.setVariable("title", subject.toString());
                context.setVariable("heading", "Welcome to Lobos, " + firstName + "!");
                context.setVariable("message", "Thanks for signing up. Please" +
                        " confirm your email address to activate your account and start organizing" +
                        " your vocabulary and grammar notes.");
                context.setVariable("buttonText", "Verify Email");
                break;
        }

        String htmlContent = templateEngine.process("mail/verification", context);
        sendHtmlEmail(to, subject.toString(), htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Sent email '{}' to {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
