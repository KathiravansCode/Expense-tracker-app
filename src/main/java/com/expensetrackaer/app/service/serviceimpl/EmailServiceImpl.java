

package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String token, String name) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            // Constructing standard front-end landing reset URL mapping
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlMsg = "<h3>Hello " + name + ",</h3>"
                    + "<p>You requested to reset your password. Please use the verification token code below or click the link to finalize your profile update.</p>"
                    + "<p><strong>Reset Token Code:</strong> " + token + "</p>"
                    + "<p><a href=\"" + resetUrl + "\">Click here to reset your password</a></p>"
                    + "<br/>"
                    + "<p><em>Note: This request link will expire in 15 minutes. If you did not make this request, please ignore this email safely.</em></p>";

            helper.setText(htmlMsg, true);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Verification Code");
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to compile or send password reset email logic", e);
        }
    }
}