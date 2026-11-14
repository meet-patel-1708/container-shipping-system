package com.shipping.fleet_service.Event;

public record PortCreatedEvent(Long portId, String portName, String portCode){
}
