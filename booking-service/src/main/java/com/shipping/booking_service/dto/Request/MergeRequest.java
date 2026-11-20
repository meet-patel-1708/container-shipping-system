package com.shipping.booking_service.dto.Request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MergeRequest(@NotNull Long targetContainerId, List<Long> bookingIds) {}
