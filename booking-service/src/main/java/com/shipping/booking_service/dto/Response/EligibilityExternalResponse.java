package com.shipping.booking_service.dto.Response;

import java.util.List;

public record EligibilityExternalResponse(boolean eligible, boolean hasAddress, boolean hasKycDocument,
                                          String kycStatus, List<String> missing) {}
