package com.shipping.fleet_service.Controller;

import com.shipping.fleet_service.DTO.Request.PortRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.PortResponse;
import com.shipping.fleet_service.Service.PortService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ports")
@RequiredArgsConstructor
public class PortController {

    private final PortService service;

    @PostMapping
    public ResponseEntity<PortResponse> create(
            @Valid @RequestBody PortRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping("/{portId}")
    public ResponseEntity<PortResponse> getById(
            @PathVariable Long portId
    ) {
        return ResponseEntity.ok(service.getById(portId));
    }

    @GetMapping("/code/{portCode}")
    public ResponseEntity<PortResponse> getByCode(
            @PathVariable String portCode
    ) {
        return ResponseEntity.ok(service.getByCode(portCode));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<PortResponse>> list(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @PutMapping("/{portCode}")
    public ResponseEntity<PortResponse> update(@PathVariable String portCode,
                                               @Valid @RequestBody PortRequest request) {
        return ResponseEntity.ok(service.update(portCode, request));
    }

    @DeleteMapping("/{portCode}")
    public ResponseEntity<Void> delete(@PathVariable String portCode) {
        service.delete(portCode);          // was service.getByCode(...)
        return ResponseEntity.noContent().build();
    }
}