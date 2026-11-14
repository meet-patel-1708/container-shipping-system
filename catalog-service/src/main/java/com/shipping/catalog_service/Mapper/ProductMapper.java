package com.shipping.catalog_service.Mapper;

import com.shipping.catalog_service.DTO.ProductRequest;
import com.shipping.catalog_service.DTO.ProductResponse;
import com.shipping.catalog_service.Entity.Product;
import org.springframework.stereotype.Component;
@Component
public class ProductMapper {

    // DTO -> Entity (ownerId is set by the service, never taken from the request)
    public Product toEntity(ProductRequest request) {
        Product product = new Product();

        product.setProductCode(request.productCode());
        product.setProductName(request.productName());
        product.setProductDescription(request.productDescription());
        product.setProductPrice(request.productPrice());
        product.setProductWeight(request.productWeight());
        product.setProductImageUrl(request.productImageUrl());

        return product;
    }

    // Update existing Entity
    public void toUpdate(ProductRequest request, Product product) {
        product.setProductCode(request.productCode());
        product.setProductName(request.productName());
        product.setProductDescription(request.productDescription());
        product.setProductPrice(request.productPrice());
        product.setProductWeight(request.productWeight());
        product.setProductImageUrl(request.productImageUrl());
    }

    // Entity -> Response DTO (unchanged)
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getProductName(),
                product.getProductDescription(),
                product.getProductPrice(),
                product.getProductWeight(),
                product.getProductImageUrl(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}