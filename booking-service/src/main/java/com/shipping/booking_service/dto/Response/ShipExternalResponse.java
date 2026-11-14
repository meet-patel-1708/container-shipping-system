package com.shipping.booking_service.dto.Response;

public record ShipExternalResponse(Long shipId, String shipCompanyName, String shipCode, Long portId) {}