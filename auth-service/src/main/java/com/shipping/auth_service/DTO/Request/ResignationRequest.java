package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResignationRequest(@NotBlank @Size(max = 500) String reason) {}
