package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.DocumentType;

import java.time.LocalDateTime;

public record KycDocumentResponse(
        Long documentId,
        DocumentType documentType,
        String documentNumber,
        LocalDateTime createdAt
) {
}
