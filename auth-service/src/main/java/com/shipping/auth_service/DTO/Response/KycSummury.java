package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.KycStatus;

import java.util.List;

public record KycSummury(KycStatus status, List<KycDocumentResponse> documents) {}
