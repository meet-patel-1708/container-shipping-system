package com.shipping.fleet_service.Controller;

import com.shipping.fleet_service.DTO.Request.ShipRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.ShipResponse;
import com.shipping.fleet_service.Service.ShipService;
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
@RequestMapping("/api/ships")
@RequiredArgsConstructor
@Tag(
        name = "Ships",
        description = "Ship management APIs"
)
public class ShipController {

    private final ShipService service;


    // =========================
    // CREATE
    // =========================

    @PostMapping
    @Operation(
            summary = "Create ship",
            description = "Create a new ship using an existing port code"
    )
    public ResponseEntity<ShipResponse> create(
            @Valid @RequestBody ShipRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.create(request));
    }


    // =========================
    // GET BY SHIP CODE
    // =========================

    @GetMapping("/{shipCode}")
    @Operation(
            summary = "Get ship by ship code"
    )
    public ResponseEntity<ShipResponse> getByCode(
            @PathVariable String shipCode
    ) {

        return ResponseEntity.ok(
                service.getByCode(shipCode)
        );
    }


    // =========================
    // LIST
    // =========================

    @GetMapping
    @Operation(
            summary = "Get all ships with pagination"
    )
    public ResponseEntity<PagedResponse<ShipResponse>> list(
            @PageableDefault(size = 20)
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                service.list(pageable)
        );
    }


    // =========================
    // SEARCH
    // =========================

    @GetMapping("/search")
    @Operation(
            summary = "Search ships by company name"
    )
    public ResponseEntity<PagedResponse<ShipResponse>> search(
            @RequestParam String companyName,
            @PageableDefault(size = 20)
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                service.searchByCompany(
                        companyName,
                        pageable
                )
        );
    }


    // =========================
    // UPDATE BY SHIP CODE
    // =========================

    @PutMapping("/{shipCode}")
    @Operation(
            summary = "Update ship by ship code"
    )
    public ResponseEntity<ShipResponse> update(
            @PathVariable String shipCode,
            @Valid @RequestBody ShipRequest request
    ) {

        return ResponseEntity.ok(
                service.update(
                        shipCode,
                        request
                )
        );
    }


    // =========================
    // DELETE BY SHIP CODE
    // =========================

    @DeleteMapping("/{shipCode}")
    @Operation(
            summary = "Delete ship by ship code"
    )
    public ResponseEntity<Void> delete(
            @PathVariable String shipCode
    ) {

        service.delete(shipCode);

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/id/{shipId}")
    @Operation(summary = "Get ship by numeric id")
    public ResponseEntity<ShipResponse> getById(@PathVariable Long shipId) {
        return ResponseEntity.ok(service.getById(shipId));
    }
}