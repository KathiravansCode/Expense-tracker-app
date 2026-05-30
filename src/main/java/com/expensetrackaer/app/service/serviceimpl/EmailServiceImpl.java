package com.expensetrackaer.app.service.serviceimpl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.expensetrackaer.app.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String token, String name) {
        try {
            Resend resend = new Resend(resendApiKey);

            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlMsg = "<h3>Hello " + name + ",</h3>"
                    + "<p>You requested to reset your password.</p>"
                    + "<p><strong>Reset Token:</strong> " + token + "</p>"
                    + "<p><a href=\"" + resetUrl + "\">Click here to reset your password</a></p>"
                    + "<p><em>This link expires in 15 minutes.</em></p>";

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("onboarding@resend.dev")
                    .to(toEmail)
                    .subject("Password Reset")
                    .html(htmlMsg)
                    .build();

            resend.emails().send(params);

        } catch (ResendException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}