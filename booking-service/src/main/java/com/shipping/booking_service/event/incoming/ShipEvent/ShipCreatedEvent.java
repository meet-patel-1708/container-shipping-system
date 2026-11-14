package com.shipping.booking_service.event.incoming.ShipEvent;

public record ShipCreatedEvent(Long shipId, String shipCompanyName, String shipCode, Long portId) {}