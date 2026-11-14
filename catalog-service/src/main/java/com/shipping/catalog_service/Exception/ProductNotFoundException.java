package com.shipping.catalog_service.Exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }
    public ProductNotFoundException(String productCode) {
        super("Product not found with code: " + productCode);
    }
}
