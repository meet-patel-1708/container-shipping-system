package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.JobStatus;

import java.time.LocalDateTime;

public record JobResponse(Long jobId, String title, String posting, String description, Long portId,
                          JobStatus status, LocalDateTime createdAt) {}
