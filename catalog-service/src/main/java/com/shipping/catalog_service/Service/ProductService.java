package com.shipping.catalog_service.Service;

import com.shipping.catalog_service.DTO.PagedResponse;
import com.shipping.catalog_service.DTO.ProductRequest;
import com.shipping.catalog_service.DTO.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse create(Long ownerId, ProductRequest request);
    ProductResponse getById(Long ownerId, Long productId);
    ProductResponse getByCode(Long ownerId, String productCode);
    PagedResponse<ProductResponse> list(Long ownerId, Pageable pageable);
    PagedResponse<ProductResponse> searchByName(Long ownerId, String name, Pageable pageable);
    ProductResponse update(Long ownerId, Long productId, ProductRequest request);
    void delete(Long ownerId, Long productId);
}