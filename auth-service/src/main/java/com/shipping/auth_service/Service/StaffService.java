package com.shipping.auth_service.Service;

import com.shipping.auth_service.DTO.Request.InviteStaffRequest;
import com.shipping.auth_service.DTO.Response.StaffResponse;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.Exception.ApiException;
import com.shipping.auth_service.Exception.EmailAlreadyExistsException;
import com.shipping.auth_service.Exception.PhoneAlreadyExistsException;
import com.shipping.auth_service.Repository.StaffInvitationRepository;
import com.shipping.auth_service.Repository.UserRepository;
import com.shipping.auth_service.entity.AccountStatus;
import com.shipping.auth_service.entity.Role;
import com.shipping.auth_service.entity.StaffInvitation;
import com.shipping.auth_service.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

    private static final int EXPIRY_HOURS = 48;
    private static final Set<Role> STAFF = EnumSet.of(Role.EMPLOYEE, Role.ADMIN);

    private final UserRepository userRepository;
    private final StaffInvitationRepository invitationRepository;
    private final EmailSenderService emailSender;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public StaffResponse invite(InviteStaffRequest req) {
        Role role = req.role();
        if (role != Role.EMPLOYEE && role != Role.ADMIN)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only EMPLOYEE or ADMIN can be invited");

        String email = req.email().trim().toLowerCase();
        String phone = req.phone().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) throw new EmailAlreadyExistsException(email);
        if (userRepository.existsByPhone(phone)) throw new PhoneAlreadyExistsException(phone);
        if (role == Role.EMPLOYEE && req.assignedPortId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Employees must be assigned to a port");
        }
        User user = userRepository.save(User.builder()
                .name(req.name().trim())
                .email(email)
                .phone(phone)
                .role(role)
                .status(AccountStatus.ACTIVE)
                .passwordHash(encoder.encode(newToken()))
                .posting(req.posting())
                .assignedPortId(role == Role.EMPLOYEE ? req.assignedPortId() : null)
                .build());

        issueInvite(user);
        return toResponse(user);
    }

    @Transactional
    public void resend(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        if (user.getRole() != Role.EMPLOYEE && user.getRole() != Role.ADMIN)
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not a staff account");
        if (user.isEmailVerified())
            throw new ApiException(HttpStatus.CONFLICT, "Invitation already accepted");
        issueInvite(user);
    }

    @Transactional
    public void accept(String token, String password) {
        StaffInvitation inv = invitationRepository.findByTokenHash(hash(token))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired invitation"));
        if (inv.getUsedAt() != null || inv.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired invitation");

        User user = inv.getUser();
        user.setPasswordHash(encoder.encode(password));
        user.setEmailVerified(true);          // the invite email proves ownership
        userRepository.save(user);

        inv.setUsedAt(LocalDateTime.now());
        invitationRepository.save(inv);
    }

    @Transactional
    public PagedResponse<StaffResponse> list(Pageable pageable) {
        return PagedResponse.from(userRepository.findByRoleIn(STAFF, pageable).map(this::toResponse));
    }

    // ---------------------------------------------------------------- helpers

    private void issueInvite(User user) {
        String raw = newToken();
        StaffInvitation inv = invitationRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> StaffInvitation.builder().user(user).build());
        inv.setTokenHash(hash(raw));
        inv.setExpiresAt(LocalDateTime.now().plusHours(EXPIRY_HOURS));
        inv.setUsedAt(null);
        invitationRepository.save(inv);

        String link = frontendUrl + "/accept-invite?token=" + raw;
        String to = user.getEmail(), name = user.getName();
        String roleLabel = user.getRole() == Role.ADMIN ? "an administrator" : "an employee";

        Runnable send = () -> {
            try {
                emailSender.sendStaffInviteEmail(to, name, roleLabel, link);
            } catch (Exception e) {
                log.error("Could not send invite to {}", to, e);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { send.run(); }
            });
        } else {
            send.run();
        }
    }

    private String newToken() {
        byte[] b = new byte[32];
        random.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private String hash(String token) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : d) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private StaffResponse toResponse(User u) {
        return new StaffResponse(u.getUserId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getRole().name(), u.isEmailVerified(),u.getPosting(),u.getAssignedPortId(), u.getCreatedAt());
    }
}
