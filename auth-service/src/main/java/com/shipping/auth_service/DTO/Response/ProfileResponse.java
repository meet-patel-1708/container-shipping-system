package com.shipping.auth_service.DTO.Response;

import java.util.List;

public record ProfileResponse(UserResponse user, List<AddressResponse> addresses,
                              KycSummury kyc, DriverSummury driver) {}
