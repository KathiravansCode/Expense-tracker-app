package com.expensetrackaer.app.service;


import org.springframework.core.io.Resource;

public interface ExportService {

    Resource exportTransactions(Long userId, int month, int year);

}
