package com.shipping.booking_service.controller;

import com.shipping.booking_service.client.StaffGuard;
import com.shipping.booking_service.dto.Request.*;
import com.shipping.booking_service.dto.Response.BookingResponse;
import com.shipping.booking_service.dto.Response.ContainerLoadResponse;
import com.shipping.booking_service.dto.Response.PagedResponse;
import com.shipping.booking_service.entity.BookingStatus;
import com.shipping.booking_service.security.UserPrinciple;
import com.shipping.booking_service.service.BookingService;
import com.shipping.booking_service.service.MovementService;
import com.shipping.booking_service.service.PortDeskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final MovementService movement;
    private final PortDeskService desk;
    private final BookingService bookings;
    private final StaffGuard guard;

    @GetMapping("/bookings")
    public PagedResponse<BookingResponse> all(@RequestParam(required = false) BookingStatus status,
                                              @PageableDefault(size = 200, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
                                              Authentication auth) {
        guard.require(auth);
        return guard.isAdmin(auth) ? bookings.listAll(status, pageable)
                : bookings.listForPort(guard.portOf(auth), status, pageable);
    }

    @GetMapping("/containers/load")
    public List<ContainerLoadResponse> load(Authentication auth) { guard.require(auth); return bookings.containerLoad(); }

    @PostMapping("/containers/{id}/ship")
    public MovementResult ship(@PathVariable Long id, @Valid @RequestBody ShipRequest b,
                               @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        guard.require(auth);
        guard.requirePort(auth, movement.portFor(id, BookingStatus.SHIPPED));
        return movement.ship(id, b.bookingIds(), b.note(), p.userId());
    }

    @PostMapping("/containers/{id}/advance")
    public MovementResult advance(@PathVariable Long id, @Valid @RequestBody AdvanceRequest b,
                                  @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        guard.require(auth);
        guard.requirePort(auth, movement.portFor(id, b.status()));
        return movement.advance(id, b.status(), b.note(), p.userId());
    }

    @PostMapping("/bookings/{id}/handover-otp")
    public ResponseEntity<Void> handoverOtp(@PathVariable Long id, Authentication auth) {
        guard.require(auth); guard.requirePort(auth, desk.portFor(id, false));
        desk.sendOtp(id, false);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/bookings/{id}/delivery-otp")
    public ResponseEntity<Void> deliveryOtp(@PathVariable Long id, Authentication auth) {
        guard.require(auth); guard.requirePort(auth, desk.portFor(id, true));
        desk.sendOtp(id, true);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/bookings/{id}/receive")
    public ResponseEntity<Void> receive(@PathVariable Long id, @Valid @RequestBody ReceiveRequest b,
                                        @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        guard.require(auth); guard.requirePort(auth, desk.portFor(id, false));
        desk.receive(id, b, p.userId());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/bookings/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @Valid @RequestBody RejectGoodsRequest b,
                                       @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        guard.require(auth); guard.requirePort(auth, desk.portFor(id, false));
        desk.reject(id, b, p.userId());
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/bookings/{id}/deliver")
    public ResponseEntity<Void> deliver(@PathVariable Long id, @Valid @RequestBody OtpVerifyRequest b,
                                        @AuthenticationPrincipal UserPrinciple p, Authentication auth) {
        guard.require(auth); guard.requirePort(auth, desk.portFor(id, true));
        desk.deliver(id, b.otp(), p.userId());
        return ResponseEntity.noContent().build();
    }
}
