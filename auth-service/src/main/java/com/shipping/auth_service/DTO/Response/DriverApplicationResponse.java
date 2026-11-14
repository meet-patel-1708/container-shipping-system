package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.ApplicantStatus;
import com.shipping.auth_service.entity.DriverDocType;
import com.shipping.auth_service.entity.DriverType;

import java.time.LocalDateTime;
import java.util.List;

public record DriverApplicationResponse(Long applicationId, Long userId, String userName, String userEmail, String userPhone,
                                        DriverType driverType, String licenseNumber, Integer experienceYears, String vehicleNumber,
                                        ApplicantStatus status, String rejectionReason,
                                        LocalDateTime submittedAt, LocalDateTime reviewedAt,
                                        List<DriverDocumentResponse> documents,
                                        List<DriverDocType> missingDocuments) {
}
