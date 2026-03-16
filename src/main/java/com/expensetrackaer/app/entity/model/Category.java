package com.expensetrackaer.app.entity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"})
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ✅ nullable = true — global categories have no user (user_id = NULL)
    // custom categories created by a user will have user_id set
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @OneToMany(mappedBy = "category")
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    private List<Budget> budgets = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Helper method ─────────────────────────────────────────────
    // Used in service layer to check if this is a global category
    public boolean isGlobal() {
        return this.user == null;
    }
}