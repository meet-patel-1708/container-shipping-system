package com.shipping.fleet_service.Controller;

import com.shipping.fleet_service.DTO.Request.VehicleRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.VehicleResponse;
import com.shipping.fleet_service.Service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService service;

    @PostMapping
    public ResponseEntity<VehicleResponse> create(
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResponse> getById(
            @PathVariable Long vehicleId
    ) {
        return ResponseEntity.ok(service.getById(vehicleId));
    }

    @GetMapping("/number/{vehicleNumber}")
    public ResponseEntity<VehicleResponse> getByNumber(
            @PathVariable String vehicleNumber
    ) {
        return ResponseEntity.ok(
                service.getByNumber(vehicleNumber)
        );
    }

    @GetMapping
    public ResponseEntity<PagedResponse<VehicleResponse>> list(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<VehicleResponse>> search(
            @RequestParam String name,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.searchByName(name, pageable)
        );
    }

    @PutMapping("/number/{vehicleNumber}")
    public ResponseEntity<VehicleResponse> update(
            @PathVariable String vehicleNumber,
            @Valid @RequestBody VehicleRequest request
    ) {
        return ResponseEntity.ok(
                service.update(vehicleNumber, request)
        );
    }

    @DeleteMapping("/{vehicleNumber}")
    public ResponseEntity<Void> delete(
            @PathVariable String vehicleNumber
    ) {
        service.delete(vehicleNumber);
        return ResponseEntity.noContent().build();
    }
}