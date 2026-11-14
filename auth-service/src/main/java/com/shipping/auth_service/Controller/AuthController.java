package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Request.*;
import com.shipping.auth_service.DTO.Response.AuthResponse;
import com.shipping.auth_service.DTO.Response.RegisterResponse;
import com.shipping.auth_service.Service.AuthService;
import com.shipping.auth_service.Service.EmailVerificationService;
import com.shipping.auth_service.Service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    private final EmailVerificationService emailVerificationService;
    private final StaffService staffService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.register(request));
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                service.login(request)
        );
    }


    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                service.refresh(
                        request.refreshToken()
                )
        );
    }


    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @Valid @RequestBody EmailVerificationRequest request) {

        emailVerificationService.verifyEmail(
                request.token()
        );

        return ResponseEntity.ok(
                "Email verified successfully. You can now login."
        );
    }


    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(
            @RequestParam String email) {

        emailVerificationService
                .resendVerificationEmail(email);

        return ResponseEntity.ok("If the account exists and is not verified, a new email has been sent.");
    }
    @PostMapping("/accept-invite")
    public ResponseEntity<String> acceptInvite(@Valid @RequestBody AcceptInviteRequest request) {
        staffService.accept(request.token(), request.password());
        return ResponseEntity.ok("Password set. You can now log in.");
    }
}