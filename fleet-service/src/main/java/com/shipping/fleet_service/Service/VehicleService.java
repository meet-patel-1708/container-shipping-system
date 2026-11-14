package com.shipping.fleet_service.Service;

import com.shipping.fleet_service.DTO.Request.VehicleRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.VehicleResponse;
import org.springframework.data.domain.Pageable;

public interface VehicleService {

    VehicleResponse create(VehicleRequest request);

    VehicleResponse getById(Long vehicleId);

    VehicleResponse getByNumber(String vehicleNumber);

    PagedResponse<VehicleResponse> list(Pageable pageable);

    PagedResponse<VehicleResponse> searchByName(
            String vehicleName,
            Pageable pageable
    );

    VehicleResponse update(String vehicleNumber, VehicleRequest request);

    void delete(String VehicleNumber);
}