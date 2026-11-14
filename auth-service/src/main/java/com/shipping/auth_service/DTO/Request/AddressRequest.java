package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "street is required")
        @Size(min = 2, max = 100) String street,

        @NotBlank(message = "city is required") @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z ]+$", message = "City should contain only letters") String city,

        @NotBlank(message = "state is required") @Size(max = 100)
        @Pattern(regexp = "^[A-Za-z ]+$", message = "State should contain only letters") String state,

        @NotBlank(message = "country is required") @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z ]+$", message = "Country should contain only letters") String country,

        @NotBlank(message = "pincode is required")
        @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid Indian pincode") String pincode
) {}
