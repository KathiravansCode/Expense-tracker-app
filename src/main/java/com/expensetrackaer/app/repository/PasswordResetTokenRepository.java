package com.expensetrackaer.app.repository;

import com.expensetrackaer.app.entity.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    // Delete all existing tokens for a user before issuing a new one
    void deleteByUserId(Long userId);
}