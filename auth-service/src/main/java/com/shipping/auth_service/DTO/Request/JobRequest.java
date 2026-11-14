package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JobRequest(@NotBlank @Size(max = 100) String title, @NotBlank @Size(max = 50) String posting,
                         @NotBlank @Size(max = 2000) String description, Long portId) {}
