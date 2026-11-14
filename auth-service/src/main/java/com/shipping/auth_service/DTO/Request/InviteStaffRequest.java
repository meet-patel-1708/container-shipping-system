package com.shipping.auth_service.DTO.Request;

import com.shipping.auth_service.entity.Role;
import jakarta.validation.constraints.*;

public record InviteStaffRequest(
        @NotBlank @Size(min = 2, max = 50) String name,
        @NotBlank @Email @Size(max = 100) String email,
        @NotBlank @Pattern(regexp = "^[6-9][0-9]{9}$",
                message = "Phone must be a valid 10-digit Indian mobile number") String phone,
        @NotNull Role role,
        @Size(max = 50) String posting,
        @NotNull Long assignedPortId) {}

