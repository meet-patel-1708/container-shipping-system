package com.shipping.fleet_service.Service;

import com.shipping.fleet_service.DTO.Request.ShipRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.ShipResponse;
import org.springframework.data.domain.Pageable;

public interface ShipService {

    ShipResponse create(ShipRequest request);

    ShipResponse getByCode(String shipCode);

    PagedResponse<ShipResponse> list(Pageable pageable);
    ShipResponse getById(Long shipId);
    PagedResponse<ShipResponse> searchByCompany(
            String companyName,
            Pageable pageable
    );

    ShipResponse update(String shipCode, ShipRequest request);

    void delete(String shipCode);
}