package com.shipping.gps_service.controller;

import com.shipping.gps_service.dto.request.LocationPingRequest;
import com.shipping.gps_service.dto.request.SimulateRequest;
import com.shipping.gps_service.dto.response.LocationResponse;
import com.shipping.gps_service.dto.response.PagedResponse;
import com.shipping.gps_service.event.LocationPingEvent;
import com.shipping.gps_service.event.LocationPingPublisher;
import com.shipping.gps_service.security.TrackingAccessService;
import com.shipping.gps_service.service.ContainerLocationService;
import com.shipping.gps_service.service.impl.MovementSimulator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class ContainerLocationController {
    private final LocationPingPublisher publisher;
    private final ContainerLocationService service;
    private final MovementSimulator simulator;
    private final TrackingAccessService access;
    @PostMapping
    public ResponseEntity<Void> recordPing(@Valid @RequestBody LocationPingRequest request){
        publisher.publish(new LocationPingEvent(
                request.containerId(),request.latitude(),request.longitude(),request.speedKmph(),request.heading()
        ));
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
    private void requireAccess(HttpServletRequest req, Long containerId) {
        String h = req.getHeader(HttpHeaders.AUTHORIZATION);
        String token = (h != null && h.startsWith("Bearer ")) ? h.substring(7).trim() : "";
        if (!access.canTrack(token, containerId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot track this container");
    }

    @GetMapping("/container/{containerId}/latest")
    public ResponseEntity<LocationResponse> getLatest(@PathVariable Long containerId, HttpServletRequest req) {
        requireAccess(req, containerId);
        return ResponseEntity.ok(service.getLatest(containerId));
    }
    // do the same at the top of getHistory(...)
    @GetMapping("/container/{containerId}/history")
    public ResponseEntity<PagedResponse<LocationResponse>> getHistory(
            @PathVariable("containerId") Long containerId,
            @PageableDefault(size = 28) Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.getHistory(containerId, pageable)
        );
    }
    @PostMapping("/simulate")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> simulate(@Valid @RequestBody SimulateRequest body) {
        simulator.start(body);
        return ResponseEntity.accepted().build();
    }

    @DeleteMapping("/simulate/{containerId}")
    @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN')")
    public ResponseEntity<Void> stopSimulation(@PathVariable Long containerId) {
        simulator.stop(containerId);
        return ResponseEntity.noContent().build();
    }
}
