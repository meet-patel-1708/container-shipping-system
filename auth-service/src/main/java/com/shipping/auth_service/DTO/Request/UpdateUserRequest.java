package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @Size(max = 50, message = "name must not exceed 50 characters")
        String name,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "phone must be a valid number")
        String phone
) {
}

