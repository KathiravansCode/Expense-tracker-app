package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService){
        this.alertService = alertService;
    }

    @GetMapping
    public Page<AlertResponse> getAlerts(
            @RequestParam Long userId,
            Pageable pageable
    ) {
        return alertService.getAlerts(userId, pageable);
    }

    @PutMapping("/{id}/read")
    public void markAlertAsRead(
            @PathVariable Long id,
            @RequestParam Long userId
    ) {
        alertService.markAlertAsRead(id, userId);
    }
}
