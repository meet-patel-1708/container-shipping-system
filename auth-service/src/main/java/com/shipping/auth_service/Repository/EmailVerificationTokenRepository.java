package com.shipping.auth_service.Repository;

import com.shipping.auth_service.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository
        extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    Optional<EmailVerificationToken> findByUser_UserId(Long userId);

    void deleteByUser_UserId(Long userId);
}