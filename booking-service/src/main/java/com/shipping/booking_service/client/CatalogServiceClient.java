package com.shipping.booking_service.client;


import com.shipping.booking_service.dto.Response.ProductExternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", url = "${services.catalog-service.url}")
public interface CatalogServiceClient {

    @GetMapping("/api/products/{productId}")
    ProductExternalResponse getProduct(@PathVariable Long productId);
}