package com.expensetrackaer.app.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {
     SseEmitter createEmitter(Long userId);

     void sendAlert(Long userId, Object alertPayload);
}
