package com.shipping.booking_service.controller;

import com.shipping.booking_service.dto.Request.BookingRequest;
import com.shipping.booking_service.dto.Response.*;
import com.shipping.booking_service.entity.BookingStatus;
import com.shipping.booking_service.security.UserPrinciple;
import com.shipping.booking_service.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Container booking management")
public class BookingController {

    private final BookingService service;

    @PostMapping
    @Operation(summary = "Create a booking for the currently authenticated user")
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserPrinciple principal
    ) {
        BookingResponse response = service.createBooking(request, principal.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long bookingId,
                                                   @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        return ResponseEntity.ok(service.getById(bookingId, p.userId(), isStaff(auth)));
    }

    @GetMapping("/{bookingId}/timeline")
    public List<TimelineEventResponse> timeline(@PathVariable Long bookingId,
                                                @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        return service.timeline(bookingId, p.userId(), isStaff(auth));
    }

    @GetMapping("/summary")
    public BookingSummaryResponse summary(@AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        return service.summary(p.userId(), isStaff(auth));
    }

    @GetMapping("/tracking/containers")
    public List<TrackedContainerResponse> tracked(@AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        return service.trackedContainers(p.userId(), isStaff(auth));
    }

    @GetMapping("/tracking-access/{containerId}")
    public TrackingAccessResponse trackingAccess(@PathVariable Long containerId,
                                                 @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        return new TrackingAccessResponse(service.canTrack(p.userId(), containerId, isStaff(auth)));
    }

    static boolean isStaff(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_EMPLOYEE"));
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long bookingId,@AuthenticationPrincipal UserPrinciple principal) {
        return ResponseEntity.ok(service.cancelBooking(bookingId,principal.userId()));
    }
    @GetMapping("/my")
    public ResponseEntity<PagedResponse<BookingResponse>> myBookings(
            @AuthenticationPrincipal UserPrinciple principal,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                service.listMyBookings(principal.userId(), status, pageable)
        );
    }
}