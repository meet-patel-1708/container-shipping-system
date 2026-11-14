package com.shipping.auth_service.DTO.Response;

import com.shipping.auth_service.entity.KycStatus;

import java.util.List;

public record EligibilityResponse(boolean eligible, boolean hasAddress, boolean hasKycDocument,
                                  KycStatus kycStatus, List<String> missing) {}
