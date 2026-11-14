package com.shipping.booking_service.mapper;


import com.shipping.booking_service.dto.Request.ContainerRequest;
import com.shipping.booking_service.dto.Response.ContainerResponse;
import com.shipping.booking_service.entity.Container;
import com.shipping.booking_service.entity.Status;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ContainerMapper {

    public Container toEntity(ContainerRequest request) {
        Container container = new Container();
        container.setContainerType(request.containerType());
        container.setContainerSize(request.containerSize());
        container.setContainerCompanyName(request.containerCompanyName());
        container.setStatus(Status.AVAILABLE);
        container.setCapacityKg(request.capacityKg() != null ? request.capacityKg()
                : BigDecimal.valueOf(request.containerSize() >= 40 ? 26000 : 21000));
        container.setAllocatedKg(BigDecimal.ZERO);
        return container;
    }

    public ContainerResponse toResponse(Container container) {
        return new ContainerResponse(
                container.getId(),
                container.getContainerType(),
                container.getContainerSize(),
                container.getContainerCompanyName(),
                container.getStatus(),
                container.getCapacityKg(), container.getAllocatedKg(), container.remainingKg(),
                container.getCreatedAt(),
                container.getUpdatedAt()
        );
    }
}
