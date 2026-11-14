package com.shipping.booking_service.event.incoming.PortEvent;

public record PortUpdatedEvent(Long portId, String portName, String portCode) {}
