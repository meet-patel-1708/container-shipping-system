package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Request.NoticeRequest;
import com.shipping.auth_service.DTO.Request.ResignationRequest;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.DTO.Response.ResignationResponse;
import com.shipping.auth_service.Security.UserPrinciple;
import com.shipping.auth_service.Service.impl.ResignationService;
import com.shipping.auth_service.entity.ResignationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resignations")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYEE')")
public class ResignationController {
    private final ResignationService service;

    @GetMapping("/me")
    public ResponseEntity<ResignationResponse> mine(@AuthenticationPrincipal UserPrinciple p) {
        ResignationResponse r = service.mine(p.userId());
        return r == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(r);
    }
    @PostMapping
    public ResignationResponse request(@AuthenticationPrincipal UserPrinciple p, @Valid @RequestBody ResignationRequest b) {
        return service.request(p.userId(), b.reason());
    }
    @DeleteMapping("/me")
    public ResignationResponse withdraw(@AuthenticationPrincipal UserPrinciple p) { return service.withdraw(p.userId()); }
}


