package com.shipping.fleet_service.Event;

public record PortUpdatedEvent(Long portId, String portName, String portCode){
}
