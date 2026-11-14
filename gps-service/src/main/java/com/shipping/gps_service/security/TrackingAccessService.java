package com.shipping.gps_service.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class TrackingAccessService {
    private final RestClient client;

    public TrackingAccessService(@Value("${services.booking-service.url}") String url) {
        this.client = RestClient.builder().baseUrl(url).build();
    }

    public boolean canTrack(String token, Long containerId) {
        try {
            Map<?, ?> r = client.get()
                    .uri("/api/bookings/tracking-access/{id}", containerId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve().body(Map.class);
            return r != null && Boolean.TRUE.equals(r.get("allowed"));
        } catch (Exception e) {
            return false;   // fail closed
        }
    }
}
