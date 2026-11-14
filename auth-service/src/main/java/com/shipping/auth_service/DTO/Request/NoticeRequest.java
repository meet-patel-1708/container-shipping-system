package com.shipping.auth_service.DTO.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record NoticeRequest(@NotNull @Min(0) @Max(180) Integer noticeDays) {}