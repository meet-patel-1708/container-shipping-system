package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Request.AddressRequest;
import com.shipping.auth_service.DTO.Request.KycDocumentRequest;
import com.shipping.auth_service.DTO.Response.AddressResponse;
import com.shipping.auth_service.DTO.Response.EligibilityResponse;
import com.shipping.auth_service.DTO.Response.KycSummury;
import com.shipping.auth_service.DTO.Response.ProfileResponse;
import com.shipping.auth_service.Security.UserPrinciple;
import com.shipping.auth_service.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService service;

    @GetMapping("/profile")
    public ProfileResponse profile(@AuthenticationPrincipal UserPrinciple p) {
        return service.profile(p.userId());
    }

    @GetMapping("/eligibility")
    public EligibilityResponse eligibility(@AuthenticationPrincipal UserPrinciple p) {
        return service.eligibility(p.userId());
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressResponse> addAddress(@AuthenticationPrincipal UserPrinciple p,
                                                      @Valid @RequestBody AddressRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAddress(p.userId(), body));
    }

    @PutMapping("/addresses/{id}")
    public AddressResponse updateAddress(@AuthenticationPrincipal UserPrinciple p,
                                         @PathVariable Long id, @Valid @RequestBody AddressRequest body) {
        return service.updateAddress(p.userId(), id, body);
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(@AuthenticationPrincipal UserPrinciple p, @PathVariable Long id) {
        service.deleteAddress(p.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/kyc/documents")
    public ResponseEntity<KycSummury> addKyc(@AuthenticationPrincipal UserPrinciple p,
                                                @Valid @RequestBody KycDocumentRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addKycDocument(p.userId(), body));
    }

    @DeleteMapping("/kyc/documents/{id}")
    public KycSummury deleteKyc(@AuthenticationPrincipal UserPrinciple p, @PathVariable Long id) {
        return service.deleteKycDocument(p.userId(), id);
    }
    @GetMapping("/kyc-verified")
    public Map<String, Boolean> kycVerified(@AuthenticationPrincipal UserPrinciple p) {
        return Map.of("verified", service.kycVerified(p.userId()));
    }
}
