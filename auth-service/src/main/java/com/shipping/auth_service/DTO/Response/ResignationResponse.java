package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.ResignationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ResignationResponse(Long resignationId, Long userId, String userName, String userEmail,
                                  String posting, Long assignedPortId, String reason, ResignationStatus status,
                                  Integer noticeDays, LocalDate lastWorkingDate, LocalDateTime requestedAt) {}