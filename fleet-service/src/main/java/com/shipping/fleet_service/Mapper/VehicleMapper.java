package com.shipping.fleet_service.Mapper;

import com.shipping.fleet_service.DTO.Request.PortRequest;
import com.shipping.fleet_service.DTO.Request.VehicleRequest;
import com.shipping.fleet_service.DTO.Response.PortResponse;
import com.shipping.fleet_service.DTO.Response.VehicleResponse;
import com.shipping.fleet_service.Entity.Port;
import com.shipping.fleet_service.Entity.Vehicle;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    public Vehicle toEntity(VehicleRequest request){
        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleName(request.vehicleName());
        vehicle.setVehicleNumber(request.vehicleNumber());
        vehicle.setVehicleCapacity(request.vehicleCapacity());
        vehicle.setVehicleType(request.vehicleType());
        return vehicle;
    }
    public void toUpdate(VehicleRequest request,Vehicle vehicle){
        vehicle.setVehicleName(request.vehicleName());
        vehicle.setVehicleNumber(request.vehicleNumber());
        vehicle.setVehicleCapacity(request.vehicleCapacity());
        vehicle.setVehicleType(request.vehicleType());
    }
    public VehicleResponse toResponse(Vehicle vehicle) {

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleName(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleCapacity(),
                vehicle.getVehicleType(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
