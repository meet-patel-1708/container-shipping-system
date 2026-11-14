package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.ApplicantStatus;
import com.shipping.auth_service.entity.DriverType;

public record DriverSummury(ApplicantStatus status, DriverType driverType, String rejectionReason) {
}
