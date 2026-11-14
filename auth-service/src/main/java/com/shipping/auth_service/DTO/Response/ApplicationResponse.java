package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.ApplicantStatus;

import java.time.LocalDateTime;

public record ApplicationResponse(Long applicationId, Long jobId, String jobTitle, String posting,
                                  Long userId, String userName, String userEmail, String userPhone,
                                  String resumeName, ApplicantStatus status, Long onboardingPortId,java.time.LocalDate joiningDate,
                                  LocalDateTime createdAt) {}
