package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HireRequest(@NotNull Long portId, @NotBlank @Size(max = 100) String portName) {}
