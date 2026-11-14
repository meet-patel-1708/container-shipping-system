package com.shipping.booking_service.client;

import com.shipping.booking_service.dto.Response.PortExternalResponse;
import com.shipping.booking_service.dto.Response.ShipExternalResponse;
import com.shipping.booking_service.dto.Response.VehicleExternalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "fleet-service", url = "${services.fleet-service.url}")
public interface FleetServiceClient {

    // matches new ShipController.getById(Long) endpoint - add that in fleet-service
    @GetMapping("/api/ships/id/{shipId}")
    ShipExternalResponse getShip(@PathVariable Long shipId);

    // matches VehicleController.getById(Long) - already correct, no fleet-service change needed
    @GetMapping("/api/vehicles/{vehicleId}")
    VehicleExternalResponse getVehicle(@PathVariable Long vehicleId);

    // matches PortController.getById(Long) - already correct, no fleet-service change needed
    @GetMapping("/api/ports/{portId}")
    PortExternalResponse getPort(@PathVariable Long portId);
}