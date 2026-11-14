package com.shipping.booking_service.client;

import com.shipping.booking_service.exception.ApiException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaffGuard {
    private final AuthServiceClient auth;

    public boolean isAdmin(Authentication a) {
        return a.getAuthorities().stream().anyMatch(x ->
                x.getAuthority().equals("ROLE_ADMIN") || x.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }

    /** staff must have admin-verified documents; admins are exempt */
    public void require(Authentication a) {
        if (isAdmin(a)) return;
        try {
            if (auth.kycVerified().verified()) return;
        } catch (FeignException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "VERIFY_FAILED", "Could not check your verification");
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "STAFF_NOT_VERIFIED",
                "An admin has not verified your documents yet");
    }

    /** the port this employee works at, or 403 */
    public Long portOf(Authentication a) {
        return a.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(s -> s.startsWith("PORT_"))
                .map(s -> Long.valueOf(s.substring(5)))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NO_PORT",
                        "No port is assigned to you yet. Ask an admin."));
    }

    public void requirePort(Authentication a, Long portId) {
        if (isAdmin(a)) return;
        if (!portOf(a).equals(portId))
            throw new ApiException(HttpStatus.FORBIDDEN, "WRONG_PORT",
                    "This task belongs to a different port. You can only handle your own port.");
    }
}