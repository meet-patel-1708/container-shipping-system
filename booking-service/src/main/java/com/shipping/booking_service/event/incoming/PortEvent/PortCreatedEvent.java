package com.shipping.booking_service.event.incoming.PortEvent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PortCreatedEvent(Long portId, String portName, String portCode) {}
