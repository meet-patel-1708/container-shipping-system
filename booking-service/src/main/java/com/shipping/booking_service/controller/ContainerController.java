package com.shipping.booking_service.controller;


import com.shipping.booking_service.dto.Request.ContainerRequest;
import com.shipping.booking_service.dto.Request.ContainerStatusRequest;
import com.shipping.booking_service.dto.Response.ContainerResponse;
import com.shipping.booking_service.dto.Response.PagedResponse;
import com.shipping.booking_service.service.ContainerService;
import com.shipping.booking_service.entity.Status;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/containers")
@RequiredArgsConstructor
@Tag(name = "Containers", description = "Container inventory management")
public class ContainerController {

    private final ContainerService service;

    @PostMapping
    @Operation(summary = "Register a new container - starts as AVAILABLE")
    public ResponseEntity<ContainerResponse> create(@Valid @RequestBody ContainerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{containerId}")
    public ResponseEntity<ContainerResponse> getById(@PathVariable Long containerId) {
        return ResponseEntity.ok(service.getById(containerId));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ContainerResponse>> list(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Filter containers by status, e.g. /api/containers/status/AVAILABLE")
    public ResponseEntity<PagedResponse<ContainerResponse>> listByStatus(
            @PathVariable Status status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(service.listByStatus(status, pageable));
    }
    @PatchMapping("/{containerId}/status")
    public ResponseEntity<ContainerResponse> updateStatus(
            @PathVariable Long containerId, @Valid @RequestBody ContainerStatusRequest body) {
        return ResponseEntity.ok(service.updateStatus(containerId, body.status()));
    }
}