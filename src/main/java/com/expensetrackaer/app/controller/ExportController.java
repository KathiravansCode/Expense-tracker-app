package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;

    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    // ✅ Removed @RequestParam Long userId — resolved from JWT in service
    @GetMapping("/transactions")
    public ResponseEntity<Resource> exportTransactions(
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        // Pass 0L as placeholder — ExportServiceImpl ignores it and uses JWT userId
        Resource file = exportService.exportTransactions(0L, month, year);

        String filename = "transactions_" + month + "_" + year + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file);
    }
}