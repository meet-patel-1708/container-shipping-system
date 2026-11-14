package com.shipping.booking_service.service;


import com.shipping.booking_service.dto.Request.ContainerRequest;
import com.shipping.booking_service.dto.Response.ContainerLoadResponse;
import com.shipping.booking_service.dto.Response.ContainerResponse;
import com.shipping.booking_service.dto.Response.PagedResponse;
import com.shipping.booking_service.entity.Status;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContainerService {

    ContainerResponse create(ContainerRequest request);

    ContainerResponse getById(Long containerId);

    PagedResponse<ContainerResponse> list(Pageable pageable);

    PagedResponse<ContainerResponse> listByStatus(Status status, Pageable pageable);
    void evictCache(Long containerId);
    public ContainerResponse updateStatus(Long containerId, Status status);

}
