package com.shipping.fleet_service.Mapper;

import com.shipping.fleet_service.DTO.Request.PortRequest;
import com.shipping.fleet_service.DTO.Response.PortResponse;
import com.shipping.fleet_service.Entity.Port;
import org.springframework.stereotype.Component;

@Component
public class PortMapper {
    public Port toEntity(PortRequest request) {
        Port port = new Port();
        port.setPortName(request.portName());
        port.setPortAddress(request.portAddress());
        port.setPortCode(request.portCode());
        port.setLatitude(request.latitude());
        port.setLongitude(request.longitude());
        return port;
    }

    public void toUpdate(PortRequest request, Port port) {
        port.setPortName(request.portName());
        port.setPortAddress(request.portAddress());
        port.setPortCode(request.portCode());
        // the current Angular form doesn't send coordinates: don't wipe them
        if (request.latitude() != null) port.setLatitude(request.latitude());
        if (request.longitude() != null) port.setLongitude(request.longitude());
    }

    public PortResponse toResponse(Port p) {
        return new PortResponse(p.getId(), p.getPortName(), p.getPortAddress(), p.getPortCode(),
                p.getCreatedAt(), p.getUpdatedAt(), p.getLatitude(), p.getLongitude());
    }
}
