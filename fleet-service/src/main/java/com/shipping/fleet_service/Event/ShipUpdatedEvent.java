package com.shipping.fleet_service.Event;

public record ShipUpdatedEvent (Long shipId,String shipCode,String shipCompanyName,Long portId){
}
