package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.service.SseEmitterService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterServiceImpl implements SseEmitterService {
    // One active SSE connection per userId
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(Long userId) {

        // Timeout of 30 minutes — frontend must reconnect after this
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        // Clean up when connection closes or times out
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        emitters.put(userId, emitter);
        return emitter;
    }

    public void sendAlert(Long userId, Object alertPayload) {

        SseEmitter emitter = emitters.get(userId);

        if (emitter != null) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("alert")
                                .data(alertPayload)
                );
            } catch (IOException e) {
                // Connection was lost — remove stale emitter
                emitters.remove(userId);
            }
        }
    }
}
