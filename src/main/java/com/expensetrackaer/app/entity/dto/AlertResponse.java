package com.expensetrackaer.app.entity.dto;

import com.expensetrackaer.app.entity.model.AlertType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertResponse {
    private Long id;

    private AlertType alertType;

    private String message;

    private Boolean isRead;

    private String categoryName;

    private LocalDateTime createdAt;

}
