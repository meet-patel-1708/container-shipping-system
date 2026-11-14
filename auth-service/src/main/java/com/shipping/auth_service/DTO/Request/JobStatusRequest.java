package com.shipping.auth_service.DTO.Request;

import com.shipping.auth_service.entity.JobStatus;
import jakarta.validation.constraints.NotNull;

public record JobStatusRequest(@NotNull JobStatus status) {}
