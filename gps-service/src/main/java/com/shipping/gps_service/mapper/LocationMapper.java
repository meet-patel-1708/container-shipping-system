package com.shipping.gps_service.mapper;

import com.shipping.gps_service.dto.response.LocationResponse;
import com.shipping.gps_service.entity.ContainerLocation;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {
    public LocationResponse toResponse(ContainerLocation location){
        return new LocationResponse(
                location.getLocationId(), location.getContainerId(), location.getLatitude(),
                location.getLongitude(), location.getSpeedKmph(),location.getHeading(), location.getRecordedAt().toInstant()
        );
    }
}
