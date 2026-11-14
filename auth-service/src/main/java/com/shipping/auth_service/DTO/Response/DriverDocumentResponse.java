package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.DriverDocType;

import java.time.LocalDateTime;

public record DriverDocumentResponse(
        Long documentId, DriverDocType documentType, String fileName,
        long sizeBytes, LocalDateTime createdAt
) {
}
