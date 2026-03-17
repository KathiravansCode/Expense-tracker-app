package  com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.security.SecurityUtils;
import com.expensetrackaer.app.service.AlertService;
import com.expensetrackaer.app.service.SseEmitterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;
    private final SseEmitterService sseEmitterService;

    @Autowired
    public AlertController(AlertService alertService,
                           SseEmitterService sseEmitterService) {
        this.alertService = alertService;
        this.sseEmitterService = sseEmitterService;
    }

    @GetMapping
    public Page<AlertResponse> getAlerts(Pageable pageable) {
        return alertService.getAlerts(pageable);
    }

    @PutMapping("/{id}/read")
    public void markAlertAsRead(@PathVariable Long id) {
        alertService.markAlertAsRead(id);
    }

    // Frontend connects to this once — backend pushes alerts in real time
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        Long userId = SecurityUtils.getCurrentUserId();
        return sseEmitterService.createEmitter(userId);
    }
}