package com.shipping.auth_service.Service;


public interface EmailVerificationService {

    void createVerificationToken(Long userId);

    void verifyEmail(String token);

    void resendVerificationEmail(String email);
}
