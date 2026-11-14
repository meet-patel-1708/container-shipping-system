package com.shipping.auth_service.DTO.Response;

import java.time.LocalDateTime;

public record AddressResponse(
        Long addressId,
        String street,
        String city,
        String state,
        String country,
        String pincode,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
