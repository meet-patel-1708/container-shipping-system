package com.shipping.booking_service.service.impl;

import com.shipping.booking_service.client.AuthServiceClient;
import com.shipping.booking_service.client.FleetServiceClient;
import com.shipping.booking_service.dto.Request.BookingItemRequest;
import com.shipping.booking_service.dto.Request.BookingRequest;
import com.shipping.booking_service.dto.Response.*;
import com.shipping.booking_service.event.BookingEventPublisher;
import com.shipping.booking_service.event.BookingTimeline;
import com.shipping.booking_service.event.outgoing.BookingCancelledEvent;
import com.shipping.booking_service.event.outgoing.BookingCreatedEvent;
import com.shipping.booking_service.exception.*;
import com.shipping.booking_service.mapper.BookingMapper;
import com.shipping.booking_service.entity.*;
import com.shipping.booking_service.entity.Replica.ProductReplica;
import com.shipping.booking_service.repository.BookedContainerRepository;
import com.shipping.booking_service.repository.BookingEventRepository;
import com.shipping.booking_service.repository.ContainerRepository;
import com.shipping.booking_service.repository.Replica.ProductReplicaRepository;
import com.shipping.booking_service.service.BookingService;
import com.shipping.booking_service.service.ContainerService;
import com.shipping.booking_service.service.ReferenceValidationService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final List<BookingStatus> OPEN = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    private static final List<BookingStatus> ACTIVE = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED,
            BookingStatus.AT_PORT, BookingStatus.SHIPPED, BookingStatus.IN_TRANSIT,BookingStatus.ARRIVED);
    private static final List<BookingStatus> MOVING = List.of(BookingStatus.SHIPPED, BookingStatus.IN_TRANSIT);
    private static final List<BookingStatus> TRACKABLE = MOVING;

    private final BookedContainerRepository bookingRepository;
    private final ContainerRepository containerRepository;
    private final ContainerService containerService;
    private final ReferenceValidationService referenceValidationService;
    private final ProductReplicaRepository productReplicaRepository;
    private final AuthServiceClient authServiceClient;
    private final BookingEventRepository eventRepository;
    private final BookingTimeline timeline;
    private final BookingMapper mapper;
    private final BookingEventPublisher eventPublisher;
    private final FleetServiceClient fleetServiceClient;

    @Override
    @Transactional()
    public PagedResponse<BookingResponse> listForPort(Long portId, Pageable pageable) {
        return PagedResponse.from(
                bookingRepository.findByOriginPortIdOrDestPortId(portId, portId, pageable).map(mapper::toResponse));
    }
    @Override
    @Transactional
    @CacheEvict(value = "myBookings", key = "#userId")
    public BookingResponse createBooking(BookingRequest request, Long userId) {
        requireEligible();
        Container container = containerRepository.findByIdForUpdate(request.containerId())
                .orElseThrow(() -> new ContainerNotFoundException(request.containerId()));
        if (container.getStatus() != Status.AVAILABLE)
            throw new ContainerNotAvailableException(request.containerId());

        if (!referenceValidationService.isShipValid(request.shipId()))
            throw new InvalidReferenceException("Ship", request.shipId());
        if (!referenceValidationService.isPortValid(request.originPortId()))
            throw new InvalidReferenceException("Origin port", request.originPortId());
        if (!referenceValidationService.isPortValid(request.destPortId()))
            throw new InvalidReferenceException("Destination port", request.destPortId());

        BookedContainer booking = new BookedContainer();
        booking.setContainer(container);
        booking.setUserId(userId);
        booking.setShipId(request.shipId());
        booking.setOriginPortId(request.originPortId());
        booking.setDestPortId(request.destPortId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setScheduleAt(request.scheduleAt());

        BigDecimal weight = BigDecimal.ZERO;
        for (BookingItemRequest ir : request.items()) {
            if (!referenceValidationService.isProductValid(ir.productId()))
                throw new InvalidReferenceException("Product", ir.productId());
            ProductReplica product = productReplicaRepository.findById(ir.productId())
                    .orElseThrow(() -> new InvalidReferenceException("Product", ir.productId()));
            if (product.getProductWeight() == null)
                throw new ApiException(HttpStatus.BAD_REQUEST, "NO_WEIGHT",
                        "Product " + product.getProductName() + " has no weight configured");
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            item.setProductId(product.getProductId());
            item.setQuantity(ir.quantity());
            item.setUnitPrice(product.getProductPrice());
            item.setProductName(product.getProductName());
            booking.getItems().add(item);
            weight = weight.add(product.getProductWeight().multiply(BigDecimal.valueOf(ir.quantity())));
        }

        BigDecimal remaining = container.remainingKg();
        if (weight.compareTo(remaining) > 0)
            throw new ApiException(HttpStatus.CONFLICT, "CONTAINER_FULL",
                    "This container has only " + remaining.stripTrailingZeros().toPlainString()
                            + " kg left, but your products weigh " + weight.stripTrailingZeros().toPlainString() + " kg");
        List<BookedContainer> open = bookingRepository.findByContainer_IdAndStatusIn(container.getId(), OPEN);
        if (!open.isEmpty()) {
            BookedContainer first = open.get(0);
            if (!first.getShipId().equals(request.shipId())
                    || !first.getOriginPortId().equals(request.originPortId())
                    || !first.getDestPortId().equals(request.destPortId()))
                throw new ApiException(HttpStatus.CONFLICT, "ROUTE_MISMATCH",
                        "This container is already booked on a different ship or route.");
        }

        booking.setTotalWeightKg(weight);
        container.setAllocatedKg(container.getAllocatedKg().add(weight));
        containerRepository.save(container);
        containerService.evictCache(container.getId());

        BookedContainer saved = bookingRepository.save(booking);
        timeline.record(saved, BookingStatus.PENDING, "Booking created", userId);

        BigDecimal total = saved.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        eventPublisher.publishBookingCreated(new BookingCreatedEvent(
                saved.getId(), saved.getUserId(), container.getId(), saved.getShipId(), total, null, null));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional()
    public BookingResponse getById(Long bookingId, Long requesterId, boolean staff) {
        return mapper.toResponse(load(bookingId, requesterId, staff));
    }

    @Override
    @Transactional()
    public PagedResponse<BookingResponse> listMyBookings(Long userId, BookingStatus status, Pageable pageable) {
        Page<BookedContainer> page = (status == null)
                ? bookingRepository.findByUserId(userId, pageable)
                : bookingRepository.findByUserIdAndStatus(userId, status, pageable);
        return PagedResponse.from(page.map(mapper::toResponse));
    }

    @Override
    @Transactional()
    public PagedResponse<BookingResponse> listAll(BookingStatus status, Pageable pageable) {
        Page<BookedContainer> page = (status == null)
                ? bookingRepository.findAll(pageable)
                : bookingRepository.findByStatus(status, pageable);
        return PagedResponse.from(page.map(mapper::toResponse));
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long bookingId, Long requesterId) {
        BookedContainer booking = load(bookingId, requesterId, false);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ApiException(HttpStatus.CONFLICT, "NOT_CANCELLABLE",
                    "Only unpaid bookings can be cancelled. Contact support for a paid booking.");
        }
        Container container = containerRepository.findByIdForUpdate(booking.getContainer().getId())
                .orElseThrow(() -> new ContainerNotFoundException(booking.getContainer().getId()));
        container.release(booking.getTotalWeightKg());
        containerRepository.save(container);
        containerService.evictCache(container.getId());

        booking.setStatus(BookingStatus.CANCELLED);
        BookedContainer saved = bookingRepository.save(booking);
        timeline.record(saved, BookingStatus.CANCELLED, "Cancelled by customer", requesterId);
        eventPublisher.publishBookingCancelled(new BookingCancelledEvent(
                saved.getId(), saved.getUserId(), container.getId()));
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional()
    public List<TimelineEventResponse> timeline(Long bookingId, Long requesterId, boolean staff) {
        load(bookingId, requesterId, staff);
        return eventRepository.findByBooking_IdOrderByCreatedAtAsc(bookingId).stream()
                .map(e -> new TimelineEventResponse(e.getStatus().name(), e.getNote(), e.getCreatedAt()))
                .toList();
    }

    @Override
    @Transactional()
    public BookingSummaryResponse summary(Long userId, boolean staff) {
        if (staff) {
            return new BookingSummaryResponse(
                    bookingRepository.countByStatusIn(ACTIVE),
                    bookingRepository.countAllDistinctContainers(MOVING),
                    bookingRepository.countByStatus(BookingStatus.PENDING));
        }
        return new BookingSummaryResponse(
                bookingRepository.countByUserIdAndStatusIn(userId, ACTIVE),
                bookingRepository.countDistinctContainers(userId, MOVING),
                bookingRepository.countByUserIdAndStatus(userId, BookingStatus.PENDING));
    }

    @Override
    @Transactional()
    public List<TrackedContainerResponse> trackedContainers(Long userId, boolean staff) {
        List<BookedContainer> list = staff
                ? bookingRepository.findByStatusIn(TRACKABLE)
                : bookingRepository.findByUserIdAndStatusIn(userId, TRACKABLE);
        Map<Long, BookedContainer> byContainer = new LinkedHashMap<>();
        for (BookedContainer b : list) byContainer.putIfAbsent(b.getContainer().getId(), b);
        return byContainer.values().stream()
                .map(b -> new TrackedContainerResponse(b.getContainer().getId(), b.getStatus().name(),
                        b.getOriginPortId(), b.getDestPortId(), b.getShipId()))
                .toList();
    }

    @Override
    @Transactional()
    public boolean canTrack(Long userId, Long containerId, boolean staff) {
        return staff || bookingRepository.existsByUserIdAndContainer_IdAndStatusIn(userId, containerId, TRACKABLE);
    }

    // ---------------------------------------------------------------- helpers

    private BookedContainer load(Long bookingId, Long requesterId, boolean staff) {
        BookedContainer b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        if (!staff && !b.getUserId().equals(requesterId)) throw new BookingAccessDeniedException();
        return b;
    }

    private void requireEligible() {
        EligibilityExternalResponse e;
        try {
            e = authServiceClient.eligibility();
        } catch (FeignException ex) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PROFILE_CHECK_FAILED",
                    "Could not verify your profile. Please try again shortly.");
        }
        if (!e.eligible()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PROFILE_INCOMPLETE",
                    "Complete your profile before booking. Missing: " + String.join(", ", e.missing()));
        }
    }

    private PortExternalResponse originPort(Long id) {
        PortExternalResponse p;
        try {
            p = fleetServiceClient.getPort(id);
        } catch (FeignException e) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "PORT_LOOKUP_FAILED",
                    "Could not calculate the pickup charge. Please try again shortly.");
        }
        if (p.latitude() == null || p.longitude() == null)
            throw new ApiException(HttpStatus.CONFLICT, "PORT_NO_COORDS",
                    "The origin port has no coordinates yet. Ask an admin to add them in Fleet.");
        return p;
    }
    @Override
    @Transactional()
    public PagedResponse<BookingResponse> listForPort(Long portId, BookingStatus status, Pageable pageable) {
        Page<BookedContainer> page = status == null
                ? bookingRepository.findByOriginPortIdOrDestPortId(portId, portId, pageable)
                : bookingRepository.findByStatusAndOriginPortIdOrStatusAndDestPortId(status, portId, status, portId, pageable);
        return PagedResponse.from(page.map(mapper::toResponse));
    }
    @Override
    @Transactional()
    public List<ContainerLoadResponse> containerLoad() {
        return containerRepository.findByAllocatedKgGreaterThan(BigDecimal.ZERO).stream()
                .map(c -> new ContainerLoadResponse(c.getId(), c.getCapacityKg(), c.getAllocatedKg(), c.remainingKg())).toList();
    }
}