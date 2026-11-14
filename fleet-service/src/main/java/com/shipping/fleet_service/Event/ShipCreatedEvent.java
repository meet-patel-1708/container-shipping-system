package com.shipping.fleet_service.Event;

public record ShipCreatedEvent (Long shipId,String shipCode,String shipCompanyName,Long portId){
}
