package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.entity.dto.AlertResponse;
import com.expensetrackaer.app.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // ✅ Removed @RequestParam Long userId — userId now comes from JWT in the service
    @GetMapping
    public Page<AlertResponse> getAlerts(Pageable pageable) {
        return alertService.getAlerts(pageable);
    }

    // ✅ Removed @RequestParam Long userId — userId now comes from JWT in the service
    @PutMapping("/{id}/read")
    public void markAlertAsRead(@PathVariable Long id) {
        alertService.markAlertAsRead(id);
    }
}