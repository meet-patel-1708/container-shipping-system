package com.shipping.catalog_service.Exception;

public class ProductCodeAlreadyExistException extends RuntimeException {
    public ProductCodeAlreadyExistException(String productCode) {
        super("Product not found with code: " + productCode);
    }
}   
