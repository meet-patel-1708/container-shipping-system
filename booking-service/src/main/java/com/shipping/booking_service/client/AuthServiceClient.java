package com.shipping.booking_service.client;

import com.shipping.booking_service.dto.Response.EligibilityExternalResponse;
import com.shipping.booking_service.dto.Response.KycVerifiedResponse;
import com.shipping.booking_service.dto.Response.UserExternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "auth-service", url = "${services.auth-service.url}")
public interface AuthServiceClient {
    @GetMapping("/api/users/{userId}")
    UserExternalResponse getuser(@PathVariable Long userId);
    @GetMapping("/api/users/me/eligibility")
    EligibilityExternalResponse eligibility();
    @GetMapping("/api/users/me/kyc-verified")
    KycVerifiedResponse kycVerified();
}
