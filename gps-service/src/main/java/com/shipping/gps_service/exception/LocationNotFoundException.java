package com.shipping.gps_service.exception;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException(Long containerId){
        super("No Location Data Found For Container id:"+containerId);
    }
}
