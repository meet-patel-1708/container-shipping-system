package com.shipping.gps_service.service;

import com.shipping.gps_service.dto.response.LocationResponse;
import com.shipping.gps_service.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

public interface ContainerLocationService {
    LocationResponse getLatest(Long containerId);
    PagedResponse<LocationResponse> getHistory(Long containerId, Pageable pageable);
}
