package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Request.KycStatusUpdateRequest;
import com.shipping.auth_service.DTO.Response.AdminKycResponse;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.Service.ProfileService;
import com.shipping.auth_service.entity.KycStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/kyc")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminKycController {

    private final ProfileService service;

    @GetMapping
    public PagedResponse<AdminKycResponse> list(@RequestParam(required = false) KycStatus status,
                                                @PageableDefault(size = 20) Pageable pageable) {
        return service.listKyc(status, pageable);
    }

    @PutMapping("/{kycId}/status")
    public ResponseEntity<Void> setStatus(@PathVariable Long kycId,
                                          @Valid @RequestBody KycStatusUpdateRequest body) {
        service.setKycStatus(kycId, body.status());
        return ResponseEntity.noContent().build();
    }
}
