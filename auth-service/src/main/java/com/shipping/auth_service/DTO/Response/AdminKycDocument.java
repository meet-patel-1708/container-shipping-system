package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.DocumentType;

public record AdminKycDocument(Long documentId, DocumentType documentType, String documentNumber) {}
