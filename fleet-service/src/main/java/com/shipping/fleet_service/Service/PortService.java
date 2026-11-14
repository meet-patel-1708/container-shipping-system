package com.shipping.fleet_service.Service;


import com.shipping.fleet_service.DTO.Request.PortRequest;
import com.shipping.fleet_service.DTO.Response.PagedResponse;
import com.shipping.fleet_service.DTO.Response.PortResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PortService {
    PortResponse create(PortRequest request);
    PortResponse getById(Long id);
    PortResponse getByCode(String portCode);

    PagedResponse<PortResponse> list(Pageable pageable);
    PortResponse getPortByName(String portName);

    PortResponse update(String portCode, PortRequest request);

    void delete(String portCode);
}
