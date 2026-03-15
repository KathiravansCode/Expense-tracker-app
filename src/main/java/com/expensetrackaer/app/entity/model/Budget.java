package com.expensetrackaer.app.entity.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="budgets",uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"user_id", "category_id", "month", "year"}
        )
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="budget_month",nullable=false)
    private Month month;

    @Column(name="budget_year",nullable=false)
    private Integer year;

    @Column(name="limit_amount",nullable=false,precision = 15, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable=false,updatable = false,name="created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @PrePersist
    public void onCreate(){
        this.createdAt=LocalDateTime.now();
    }

}
