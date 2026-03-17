package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.model.Transaction;
import com.expensetrackaer.app.entity.model.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {

    // Builds a dynamic query — only adds conditions for non-null parameters
    // This avoids PostgreSQL's "could not determine data type of parameter" error
    // which happens when null parameters are used in IS NULL checks in JPQL
    public static Specification<Transaction> filterBy(
            Long userId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Always filter by the current user — never optional
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Only add type filter if type is provided
            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }

            // Only add date filters if dates are provided
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("transactionDate"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("transactionDate"), endDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}