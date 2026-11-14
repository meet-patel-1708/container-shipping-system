package com.shipping.catalog_service.Controller;

import com.shipping.catalog_service.DTO.PagedResponse;
import com.shipping.catalog_service.DTO.ProductRequest;
import com.shipping.catalog_service.DTO.ProductResponse;
import com.shipping.catalog_service.Service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService service;

    private Long ownerId(Authentication auth) {
        return Long.valueOf(auth.getName());   // JwtAuthFilter sets name = JWT subject = userId
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(
            Authentication auth, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(ownerId(auth), request));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getById(Authentication auth, @PathVariable Long productId) {
        return ResponseEntity.ok(service.getById(ownerId(auth), productId));
    }

    @GetMapping("/code/{productCode}")
    public ResponseEntity<ProductResponse> getByCode(Authentication auth, @PathVariable String productCode) {
        return ResponseEntity.ok(service.getByCode(ownerId(auth), productCode));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> list(
            Authentication auth, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.list(ownerId(auth), pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<ProductResponse>> search(
            Authentication auth, @RequestParam String name, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.searchByName(ownerId(auth), name, pageable));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponse> update(
            Authentication auth, @PathVariable Long productId, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(service.update(ownerId(auth), productId, request));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long productId) {
        service.delete(ownerId(auth), productId);
        return ResponseEntity.noContent().build();
    }
}