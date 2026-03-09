package com.expensetrackaer.app.entity.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="transactions", indexes = {
        @Index(name = "idx_txn_user", columnList = "user_id"),
        @Index(name = "idx_txn_date", columnList = "transaction_date"),
        @Index(name = "idx_txn_category", columnList = "category_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false,precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = true,columnDefinition = "TEXT")
    private String description;

    @Column(nullable=false,name="transaction_date")
    private LocalDate transactionDate;


    @Enumerated(EnumType.STRING)
    @Column(name="payment_mode")
    private PaymentMode paymentMode;

    @Column(nullable=false,name="is_unusual")
    private Boolean isUnusual=false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(name="created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="category_id",nullable=false)
    private Category category;

    @PrePersist
    public void onCreate(){

        this.createdAt=LocalDateTime.now();
    }


}
