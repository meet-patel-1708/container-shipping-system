package com.shipping.booking_service.dto.Response;

public record TrackedContainerResponse(Long containerId, String status,
                                       Long originPortId, Long destPortId, Long shipId) {}
