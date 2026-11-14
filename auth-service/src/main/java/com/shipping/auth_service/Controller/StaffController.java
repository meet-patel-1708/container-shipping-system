package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Request.InviteStaffRequest;
import com.shipping.auth_service.DTO.Response.StaffResponse;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.Service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class StaffController {

    private final StaffService service;

    @GetMapping
    public ResponseEntity<PagedResponse<StaffResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @PostMapping
    public ResponseEntity<StaffResponse> invite(@Valid @RequestBody InviteStaffRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.invite(body));
    }

    @PostMapping("/{userId}/resend")
    public ResponseEntity<Void> resend(@PathVariable Long userId) {
        service.resend(userId);
        return ResponseEntity.noContent().build();
    }

    private static boolean isSuper(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
