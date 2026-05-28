package com.expensetrackaer.app.entity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable= false,unique = true)
    private String email;

    @Column(nullable=false)
    private String password;

    @OneToMany(mappedBy ="user")
    private List<Category> categories=new ArrayList<>();

    @OneToMany(mappedBy="user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions=new ArrayList<>();

    @OneToMany(mappedBy="user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets=new ArrayList<>();

    @OneToMany(mappedBy="user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alert> alerts=new ArrayList<>();

    // ── Fix: cascade deletes to password_reset_tokens ─────────────
    // Without this, deleting a user whose password was reset via
    // forgot-password fails with:
    //   ERROR: update or delete on table "users" violates foreign key
    //   constraint on table "password_reset_tokens"
    // Adding cascade ALL + orphanRemoval ensures Hibernate deletes all
    // associated reset tokens before removing the user row.
    @OneToMany(mappedBy="user",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordResetToken> passwordResetTokens=new ArrayList<>();

    @Column(name="created_at",nullable=false,updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt=LocalDateTime.now();
    }
}