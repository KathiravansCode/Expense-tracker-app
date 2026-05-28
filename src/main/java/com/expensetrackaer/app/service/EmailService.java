package com.expensetrackaer.app.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String token, String name);
}