package com.expensetrackaer.app.entity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="alerts", indexes = {
        @Index(name = "idx_alert_user", columnList = "user_id")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String message;

    @Column(name="is_read",nullable = false)
    private Boolean isRead;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private AlertType alertType;

    @Column(name="created_at",nullable=false,updatable=false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable=false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;


    @PrePersist
    public void onCreate(){

        this.createdAt=LocalDateTime.now();
    }
}
