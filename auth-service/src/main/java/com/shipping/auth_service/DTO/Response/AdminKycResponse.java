package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.KycStatus;
import java.util.List;

public record AdminKycResponse(Long kycId, Long userId, String userName, String userEmail,
                               KycStatus status, List<AdminKycDocument> documents, String userRole) {}