package com.shipping.auth_service.Service.impl;

import com.shipping.auth_service.Repository.EmailVerificationTokenRepository;
import com.shipping.auth_service.Repository.UserRepository;
import com.shipping.auth_service.Service.EmailSenderService;
import com.shipping.auth_service.Service.EmailVerificationService;
import com.shipping.auth_service.entity.EmailVerificationToken;
import com.shipping.auth_service.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailSenderService emailSenderService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRATION_MINUTES = 15;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void createVerificationToken(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            return;
        }

        String rawToken = generateRawToken();
        String tokenHash = hashToken(rawToken);

        // Reuse the existing row (user_id is UNIQUE), or create a new one
        EmailVerificationToken token = tokenRepository.findByUser_UserId(userId)
                .orElseGet(() -> EmailVerificationToken.builder().user(user).build());

        token.setTokenHash(tokenHash);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES));
        token.setUsedAt(null);
        tokenRepository.save(token);

        String verificationLink = frontendUrl + "/verify-email?token=" + rawToken;

        // DEV ONLY: remove before production (it writes a live token to the logs)
        log.info("DEV verification link for {}: {}", user.getEmail(), verificationLink);

        final String email = user.getEmail();
        final String name = user.getName();

        Runnable send = () -> {
            try {
                emailSenderService.sendVerificationEmail(email, name, verificationLink);
            } catch (Exception e) {
                log.error("Could not send verification email to {}", email, e);
            }
        };

        // Send only after the DB transaction has committed
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            send.run();
                        }
                    }
            );
        } else {
            send.run();
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Verification token is required");
        }

        String tokenHash = hashToken(token);

        EmailVerificationToken verificationToken =
                tokenRepository.findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Invalid verification token"));

        if (verificationToken.getUsedAt() != null) {
            throw new IllegalArgumentException("Verification token has already been used");
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        User user = verificationToken.getUser();

        if (user.isEmailVerified()) {
            return;
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {

        userRepository
                .findByEmailIgnoreCase(email.trim().toLowerCase())
                .filter(u -> !u.isEmailVerified())
                .ifPresent(u -> createVerificationToken(u.getUserId()));
    }

    private String generateRawToken() {

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}