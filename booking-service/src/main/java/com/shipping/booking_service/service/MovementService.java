package com.shipping.booking_service.service;

import com.shipping.booking_service.dto.Request.MovementResult;
import com.shipping.booking_service.entity.BookedContainer;
import com.shipping.booking_service.entity.BookingStatus;
import com.shipping.booking_service.entity.Container;
import com.shipping.booking_service.entity.Status;
import com.shipping.booking_service.event.BookingEventPublisher;
import com.shipping.booking_service.event.BookingTimeline;
import com.shipping.booking_service.event.outgoing.BookingCancelledEvent;
import com.shipping.booking_service.event.outgoing.BookingStatusChangedEvent;
import com.shipping.booking_service.exception.ContainerNotFoundException;
import com.shipping.booking_service.repository.BookedContainerRepository;
import com.shipping.booking_service.repository.ContainerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.shipping.booking_service.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementService {
    private static final Map<BookingStatus, BookingStatus> PREVIOUS =
            Map.of(BookingStatus.IN_TRANSIT, BookingStatus.SHIPPED, BookingStatus.ARRIVED, BookingStatus.IN_TRANSIT);
    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.CONFIRMED, BookingStatus.AT_PORT,
            BookingStatus.SHIPPED, BookingStatus.IN_TRANSIT, BookingStatus.ARRIVED);

    private final BookedContainerRepository bookingRepository;
    private final ContainerRepository containerRepository;
    private final ContainerService containerService;
    private final BookingTimeline timeline;
    private final BookingEventPublisher publisher;

    @Transactional()
    public Long portFor(Long containerId, BookingStatus target) {
        List<BookedContainer> list = bookingRepository.findByContainer_IdAndStatusIn(containerId, ACTIVE);
        if (list.isEmpty()) throw new ApiException(HttpStatus.CONFLICT, "NOTHING_TO_MOVE", "No active bookings in this container");
        BookedContainer b = list.get(0);
        return target == BookingStatus.ARRIVED ? b.getDestPortId() : b.getOriginPortId();
    }

    /** IN_TRANSIT and ARRIVED move the whole voyage together */
    @Transactional
    public MovementResult advance(Long containerId, BookingStatus target, String note, Long actor) {
        BookingStatus from = PREVIOUS.get(target);
        if (from == null)
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_STATUS", "Allowed here: IN_TRANSIT, ARRIVED");
        List<BookedContainer> movers = bookingRepository.findByContainer_IdAndStatus(containerId, from);
        if (movers.isEmpty())
            throw new ApiException(HttpStatus.CONFLICT, "NOTHING_TO_MOVE", "No bookings in this container are in status " + from);
        for (BookedContainer b : movers) {
            b.setStatus(target);
            bookingRepository.save(b);
            timeline.record(b, target, note, actor);
            publisher.publishBookingStatusChanged(new BookingStatusChangedEvent(
                    b.getId(), b.getUserId(), from.name(), target.name(), null));
        }
        return new MovementResult(containerId, target, movers.size());
    }

    /** staff choose which received goods to load; everything else is offloaded */
    @Transactional
    public MovementResult ship(Long containerId, List<Long> ids, String note, Long actor) {
        Container container = containerRepository.findByIdForUpdate(containerId)
                .orElseThrow(() -> new ContainerNotFoundException(containerId));
        List<BookedContainer> all = bookingRepository.findByContainer_IdAndStatusIn(containerId,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.AT_PORT));
        Set<Long> sel = new HashSet<>(ids);
        List<BookedContainer> chosen = all.stream().filter(b -> sel.contains(b.getId())).toList();
        if (chosen.isEmpty() || chosen.size() != sel.size())
            throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_SELECTION", "Select goods that are in this container");
        if (chosen.stream().anyMatch(b -> b.getStatus() != BookingStatus.AT_PORT))
            throw new ApiException(HttpStatus.CONFLICT, "NOT_AT_PORT", "Only goods received at the port can be shipped");
        LocalDateTime due = chosen.stream().map(BookedContainer::getScheduleAt).max(Comparator.naturalOrder()).get();
        if (LocalDateTime.now().isBefore(due))
            throw new ApiException(HttpStatus.CONFLICT, "NOT_TIME_YET", "This container can ship from " + due);

        for (BookedContainer b : all) {
            if (sel.contains(b.getId())) continue;
            boolean unpaid = b.getStatus() == BookingStatus.PENDING;
            String why = unpaid ? "Not paid before shipping" : "Not loaded on this voyage";
            container.release(b.getTotalWeightKg());
            b.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(b);
            timeline.record(b, BookingStatus.CANCELLED, why, actor);
            publisher.publishBookingCancelled(new BookingCancelledEvent(b.getId(), b.getUserId(), containerId));
            publisher.publishBookingStatusChanged(new BookingStatusChangedEvent(
                    b.getId(), b.getUserId(), "PENDING", "CANCELLED", why));
        }
        for (BookedContainer b : chosen) {
            b.setStatus(BookingStatus.SHIPPED);
            bookingRepository.save(b);
            timeline.record(b, BookingStatus.SHIPPED, note == null || note.isBlank() ? "Shipped" : note, actor);
        }
        container.setStatus(Status.IN_TRANSIT);
        containerRepository.save(container);
        containerService.evictCache(containerId);
        return new MovementResult(containerId, BookingStatus.SHIPPED, chosen.size());
    }

    @Transactional
    public void markPaid(Long bookingId) {
        BookedContainer b = bookingRepository.findById(bookingId).orElse(null);
        if (b == null || b.getStatus() != BookingStatus.PENDING) { log.info("Ignoring payment for bookingId={}", bookingId); return; }
        b.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(b);
        timeline.record(b, BookingStatus.CONFIRMED, "Payment received", null);
        publisher.publishBookingStatusChanged(new BookingStatusChangedEvent(b.getId(), b.getUserId(), "PENDING", "CONFIRMED", null));
    }
}