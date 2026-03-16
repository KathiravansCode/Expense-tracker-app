package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.repository.TransactionRepository;
import com.expensetrackaer.app.security.SecurityUtils;
import com.expensetrackaer.app.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class ExportServiceImpl implements ExportService {

    private final TransactionRepository transactionRepository;

    @Autowired
    public ExportServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Resource exportTransactions(Long userId, int month, int year) {

        // ✅ Ignore the userId param passed from controller — always use JWT instead
        Long currentUserId = SecurityUtils.getCurrentUserId();

        List<Transaction> transactions =
                transactionRepository.findTransactionsForExport(currentUserId, month, year);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Category,Type,Amount,Description\n");

        for (Transaction t : transactions) {
            csv.append(t.getTransactionDate()).append(",");
            csv.append(t.getCategory().getName()).append(",");
            csv.append(t.getTransactionType()).append(",");
            csv.append(t.getAmount()).append(",");
            csv.append(
                    t.getDescription() != null
                            ? t.getDescription().replace(",", " ")
                            : ""
            ).append("\n");
        }

        return new ByteArrayResource(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
}