package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50,
                message = "Name must be between 2 and 50 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100,
                message = "Email must not exceed 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72,
                message = "Password must be between 8 and 72 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$",
                message = "Password must contain uppercase, lowercase, number and special character"
        )
        String password,

        @NotBlank(message = "Phone is required")
        @Pattern(
                regexp = "^[6-9][0-9]{9}$",
                message = "Phone must be a valid 10-digit Indian mobile number"
        )
        String phone
) {
}