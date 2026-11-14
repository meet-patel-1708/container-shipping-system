package com.shipping.auth_service.Controller;

import com.shipping.auth_service.DTO.Request.UpdateUserRequest;
import com.shipping.auth_service.DTO.Response.PagedResponse;
import com.shipping.auth_service.DTO.Response.UserResponse;
import com.shipping.auth_service.Security.UserPrinciple;
import com.shipping.auth_service.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "users",description = "User profile management")
public class UserController {
    private final UserService service;
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserPrinciple p) {
        return ResponseEntity.ok(service.getById(p.userId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> updateMe(
            @AuthenticationPrincipal UserPrinciple p,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(service.update(p.userId(), request));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> getById(@PathVariable Long userId){
        return ResponseEntity.ok(service.getById(userId));
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserResponse>> list(@PageableDefault(size = 20)Pageable pageable){
        return ResponseEntity.ok(service.list(pageable));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
            ){
        return ResponseEntity.ok(service.update(userId,request));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long userId){
        service.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
