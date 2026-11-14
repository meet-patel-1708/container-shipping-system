package com.shipping.fleet_service.Mapper;

import com.shipping.fleet_service.DTO.Request.ShipRequest;
import com.shipping.fleet_service.DTO.Response.PortResponse;
import com.shipping.fleet_service.DTO.Response.ShipResponse;
import com.shipping.fleet_service.Entity.Port;
import com.shipping.fleet_service.Entity.Ship;
import org.springframework.stereotype.Component;

@Component
public class ShipMapper {

    public Ship toEntity(ShipRequest request, Port port) {

        Ship ship = new Ship();

        ship.setShipCompanyName(request.shipCompanyName());
        ship.setShipCode(request.shipCode());
        ship.setShipCapacity(request.shipCapacity());

        // Attach existing Port entity
        ship.setPort(port);

        return ship;
    }

    public void toUpdate(
            ShipRequest request,
            Ship ship,
            Port port
    ) {

        ship.setShipCompanyName(request.shipCompanyName());
        ship.setShipCode(request.shipCode());
        ship.setShipCapacity(request.shipCapacity());

        // Replace port relationship
        ship.setPort(port);
    }

    public ShipResponse toResponse(Ship ship) {

        Port port = ship.getPort();

        PortResponse portResponse = new PortResponse(
                port.getId(), port.getPortName(), port.getPortAddress(), port.getPortCode(),
                port.getCreatedAt(), port.getUpdatedAt(), port.getLatitude(), port.getLongitude());

        return new ShipResponse(
                ship.getId(),
                ship.getShipCompanyName(),
                ship.getShipCode(),
                ship.getShipCapacity(),
                portResponse,
                ship.getCreatedAt(),
                ship.getUpdatedAt()
        );
    }
}