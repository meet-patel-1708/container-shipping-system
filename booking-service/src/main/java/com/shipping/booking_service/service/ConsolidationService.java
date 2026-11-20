package com.shipping.booking_service.service;

import com.shipping.booking_service.dto.Request.MergeRequest;
import com.shipping.booking_service.dto.Request.MovementResult;
import com.shipping.booking_service.dto.Response.MergeCandidate;
import com.shipping.booking_service.entity.BookedContainer;
import com.shipping.booking_service.entity.BookingStatus;
import com.shipping.booking_service.entity.Container;
import com.shipping.booking_service.entity.Status;
import com.shipping.booking_service.event.BookingEventPublisher;
import com.shipping.booking_service.event.outgoing.BookingStatusChangedEvent;
import com.shipping.booking_service.event.BookingTimeline;
import com.shipping.booking_service.exception.ContainerNotFoundException;
import com.shipping.booking_service.repository.BookedContainerRepository;
import com.shipping.booking_service.repository.ContainerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.shipping.booking_service.exception.ApiException;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class ConsolidationService {
    private static final List<BookingStatus> MOVABLE =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.AT_PORT);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private final BookedContainerRepository bookings;
    private final ContainerRepository containers;
    private final ContainerService containerService;
    private final BookingTimeline timeline;
    private final BookingEventPublisher publisher;
    private final CacheManager cacheManager;

    @Transactional()
    public Long originPort(Long containerId) {
        List<BookedContainer> list = bookings.findByContainer_IdAndStatusIn(containerId, MOVABLE);
        if (list.isEmpty())
            throw new ApiException(HttpStatus.CONFLICT, "NOTHING_TO_MOVE", "This container has no goods that can be moved");
        return list.get(0).getOriginPortId();
    }

    @Transactional()
    public List<MergeCandidate> candidates(Long sourceId) {
        Container source = containers.findById(sourceId).orElseThrow(() -> new ContainerNotFoundException(sourceId));
        List<BookedContainer> moving = bookings.findByContainer_IdAndStatusIn(sourceId, MOVABLE);
        if (moving.isEmpty()) return List.of();
        List<MergeCandidate> out = new ArrayList<>();
        for (Container t : containers.findByStatusAndContainerType(Status.AVAILABLE, source.getContainerType())) {
            if (t.getId().equals(sourceId)) continue;
            List<BookedContainer> open = bookings.findByContainer_IdAndStatusIn(t.getId(), MOVABLE);
            try { check(source, t, moving, open); } catch (ApiException e) { continue; }
            out.add(new MergeCandidate(t.getId(), t.getContainerType().name(), t.getContainerSize(),
                    t.getCapacityKg(), t.remainingKg(), open.size()));
        }
        return out;
    }

    @Transactional
    public MovementResult merge(Long sourceId, MergeRequest req, Long actor) {
        Long targetId = req.targetContainerId();
        if (sourceId.equals(targetId))
            throw new ApiException(HttpStatus.BAD_REQUEST, "SAME_CONTAINER", "Choose a different container");

        // lock in id order so two staff members can't deadlock each other
        Long first = Math.min(sourceId, targetId), second = Math.max(sourceId, targetId);
        Container c1 = containers.findByIdForUpdate(first).orElseThrow(() -> new ContainerNotFoundException(first));
        Container c2 = containers.findByIdForUpdate(second).orElseThrow(() -> new ContainerNotFoundException(second));
        Container source = sourceId.equals(first) ? c1 : c2;
        Container target = sourceId.equals(first) ? c2 : c1;

        List<BookedContainer> movable = bookings.findByContainer_IdAndStatusIn(sourceId, MOVABLE);
        List<BookedContainer> moving;
        if (req.bookingIds() != null && !req.bookingIds().isEmpty()) {
            Set<Long> ids = new HashSet<>(req.bookingIds());
            moving = movable.stream().filter(b -> ids.contains(b.getId())).toList();
            if (moving.size() != ids.size())
                throw new ApiException(HttpStatus.BAD_REQUEST, "BAD_SELECTION", "Select goods that are in this container");
        } else moving = movable;
        if (moving.isEmpty())
            throw new ApiException(HttpStatus.CONFLICT, "NOTHING_TO_MOVE", "Nothing to move");

        check(source, target, moving, bookings.findByContainer_IdAndStatusIn(targetId, MOVABLE));

        BigDecimal weight = moving.stream().map(BookedContainer::getTotalWeightKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (BookedContainer b : moving) {
            b.setContainer(target);
            bookings.save(b);
            timeline.record(b, b.getStatus(), "Moved from container #" + sourceId + " to #" + targetId
                    + " (consolidation). Departure unchanged.", actor);
            String msg = "Your goods for booking #" + b.getId() + " were moved from container #" + sourceId
                    + " to container #" + targetId + " because the first container was not full. "
                    + "Your departure is NOT delayed: it stays " + b.getScheduleAt().format(FMT) + ".";
            var ev = new BookingStatusChangedEvent(b.getId(), b.getUserId(), b.getStatus().name(),
                    "CONTAINER_CHANGED", msg);
            afterCommit(() -> publisher.publishBookingStatusChanged(ev));
        }

        source.release(weight);
        target.setAllocatedKg(target.getAllocatedKg().add(weight));
        containers.save(source);
        containers.save(target);
        containerService.evictCache(sourceId);
        containerService.evictCache(targetId);
        for (String name : List.of("myBookings", "bookings")) {
            Cache c = cacheManager.getCache(name);
            if (c != null) c.clear();
        }
        return new MovementResult(sourceId, moving.get(0).getStatus(), moving.size());
    }

    // ----------------------------------------------------------------- rules
    private void check(Container source, Container target, List<BookedContainer> moving, List<BookedContainer> targetOpen) {
        if (target.getStatus() != Status.AVAILABLE)
            throw new ApiException(HttpStatus.CONFLICT, "TARGET_NOT_LOADING", "The target container is not available for loading");
        if (source.getContainerType() != target.getContainerType())
            throw new ApiException(HttpStatus.CONFLICT, "TYPE_MISMATCH", "Goods can move only to the same type of container");

        BookedContainer ref = moving.get(0);
        for (BookedContainer b : targetOpen)
            if (!sameRoute(b, ref))
                throw new ApiException(HttpStatus.CONFLICT, "ROUTE_MISMATCH", "The target container is on a different ship or route");

        LocalDate day = ref.getScheduleAt().toLocalDate();
        boolean sameDay = Stream.concat(moving.stream(), targetOpen.stream())
                .allMatch(b -> b.getScheduleAt().toLocalDate().equals(day));
        if (!sameDay)
            throw new ApiException(HttpStatus.CONFLICT, "DEPARTURE_DIFFERS",
                    "Goods can be merged only when everything departs on the same day, so nobody is delayed");

        BigDecimal weight = moving.stream().map(BookedContainer::getTotalWeightKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (weight.compareTo(target.remainingKg()) > 0)
            throw new ApiException(HttpStatus.CONFLICT, "CONTAINER_FULL",
                    "The target has only " + target.remainingKg().stripTrailingZeros().toPlainString() + " kg free");
    }

    private static boolean sameRoute(BookedContainer a, BookedContainer b) {
        return a.getShipId().equals(b.getShipId()) && a.getOriginPortId().equals(b.getOriginPortId())
                && a.getDestPortId().equals(b.getDestPortId());
    }

    private static void afterCommit(Runnable r) {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { r.run(); }
            });
        else r.run();
    }
}
