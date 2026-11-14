package com.shipping.booking_service.event.incoming.ShipEvent;

public record ShipUpdatedEvent(Long shipId, String shipCompanyName, String shipCode, Long portId) {}