package com.expensetrackaer.app.service.serviceimpl;

import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.repository.TransactionRepository;
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

    private Long getUserId(){
        return 1L;
    }
    @Override
        public Resource exportTransactions(Long userId, int month, int year) {

            List<Transaction> transactions =
                    transactionRepository.findTransactionsForExport(getUserId(), month, year);

            StringBuilder csv = new StringBuilder();

            // Header
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

            return new ByteArrayResource(
                    csv.toString().getBytes(StandardCharsets.UTF_8)
            );
        }
    }

