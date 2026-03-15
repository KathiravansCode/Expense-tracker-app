package com.expensetrackaer.app.controller;

import com.expensetrackaer.app.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.io.Resource;
@RestController
@RequestMapping("/api/export")
public class ExportController {
    private final ExportService exportService;

    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<Resource> exportTransactions(
            @RequestParam Long userId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {

        Resource file =
                exportService.exportTransactions(userId, month, year);

        String filename = "transactions_" + month + "_" + year + ".csv";

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + filename
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file);
    }
}
