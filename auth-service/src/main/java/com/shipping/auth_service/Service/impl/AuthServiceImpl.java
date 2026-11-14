package com.shipping.auth_service.Service.impl;

import com.shipping.auth_service.DTO.Request.LoginRequest;
import com.shipping.auth_service.DTO.Request.RegisterRequest;
import com.shipping.auth_service.DTO.Response.AuthResponse;
import com.shipping.auth_service.DTO.Response.RegisterResponse;
import com.shipping.auth_service.Event.UserEventPublisher;
import com.shipping.auth_service.Event.UserRegisterEvent;
import com.shipping.auth_service.Exception.EmailAlreadyExistsException;
import com.shipping.auth_service.Exception.EmailNotVerifiedException;
import com.shipping.auth_service.Exception.InvalidCredentialsException;
import com.shipping.auth_service.Exception.PhoneAlreadyExistsException;
import com.shipping.auth_service.Mapper.UserMapper;
import com.shipping.auth_service.Repository.KycRepository;
import com.shipping.auth_service.Repository.UserRepository;
import com.shipping.auth_service.Security.JwtService;
import com.shipping.auth_service.Service.AuthService;
import com.shipping.auth_service.Service.EmailVerificationService;
import com.shipping.auth_service.entity.AccountStatus;
import com.shipping.auth_service.entity.Kyc;
import com.shipping.auth_service.entity.KycStatus;
import com.shipping.auth_service.entity.User;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final UserEventPublisher publisher;
    private final KycRepository kycRepository;
    private final EmailVerificationService emailVerificationService;
    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        String phone = request.phone()
                .trim();

        // --------------------------------------------------
        // 1. CHECK EMAIL
        // --------------------------------------------------

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        // --------------------------------------------------
        // 2. CHECK PHONE
        // --------------------------------------------------

        if (repository.existsByPhone(phone)) {
            throw new PhoneAlreadyExistsException(phone);
        }

        // --------------------------------------------------
        // 3. CREATE USER
        // --------------------------------------------------

        User user = mapper.toEntity(request);

        // NEVER accept role from frontend
        user.setRole(com.shipping.auth_service.entity.Role.USER);

        // BCrypt password hashing
        user.setPasswordHash(
                encoder.encode(request.password())
        );

        // Account is active, but email/phone are initially
        // unverified.
        user.setStatus(
                com.shipping.auth_service.entity.AccountStatus.ACTIVE
        );

        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        // --------------------------------------------------
        // 4. SAVE USER
        // --------------------------------------------------

        User savedUser = repository.save(user);

        // --------------------------------------------------
        // 5. CREATE KYC
        // --------------------------------------------------

        Kyc kyc = Kyc.builder()
                .user(savedUser)
                .status(KycStatus.PENDING)
                .build();

        kycRepository.save(kyc);
        emailVerificationService.createVerificationToken(
                savedUser.getUserId()
        );
        // --------------------------------------------------
        // 6. PUBLISH USER REGISTERED EVENT
        // --------------------------------------------------

        publisher.publishUserRegistered(
                new UserRegisterEvent(
                        savedUser.getUserId(),
                        savedUser.getName(),
                        savedUser.getEmail(),
                        savedUser.getCreatedAt(),
                        savedUser.getPhone()
                )
        );

        // --------------------------------------------------
        // 7. RETURN REGISTRATION RESPONSE
        // --------------------------------------------------

        return new RegisterResponse(
                savedUser.getUserId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getRole().name(),
                savedUser.getStatus().name(),
                "Registration successful. Please verify your email before login."
        );
    }

    @Override
    @RateLimiter(name = "loginRateLimiter")
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase();

        User user = repository
                .findByEmailIgnoreCase(email)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        // --------------------------------------------------
        // PASSWORD
        // --------------------------------------------------

        if (!encoder.matches(
                request.password(),
                user.getPasswordHash()
        )) {
            throw new InvalidCredentialsException();
        }

        // --------------------------------------------------
        // ACCOUNT STATUS
        // --------------------------------------------------

        if (user.getStatus()
                == com.shipping.auth_service.entity.AccountStatus.BLOCKED) {

            throw new InvalidCredentialsException();
        }

        if (user.getStatus()
                == com.shipping.auth_service.entity.AccountStatus.SUSPENDED) {

            throw new InvalidCredentialsException();
        }

        // --------------------------------------------------
        // EMAIL VERIFICATION
        // --------------------------------------------------

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email Not Verified");
        }

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {

        var claims = jwtService.extractClaims(refreshToken);

        if (claims == null) {
            throw new InvalidCredentialsException();
        }

        if (!"refresh".equals(
                claims.get("type", String.class)
        )) {
            throw new InvalidCredentialsException();
        }

        Long userId;

        try {
            userId = Long.valueOf(
                    claims.getSubject()
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException();
        }

        User user = repository
                .findById(userId)
                .orElseThrow(
                        InvalidCredentialsException::new
                );

        if (user.getStatus() != AccountStatus.ACTIVE || !user.isEmailVerified()) {
            throw new InvalidCredentialsException();
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                jwtService.generateRefreshToken(user);

        return AuthResponse.of(
                accessToken,
                refreshToken,
                "Bearer",
                jwtService.getAccessTokenExpirationMs(),
                mapper.toResponse(user)
        );
    }

}