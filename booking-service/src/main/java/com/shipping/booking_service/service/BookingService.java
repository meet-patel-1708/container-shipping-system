package com.shipping.booking_service.service;

import com.shipping.booking_service.dto.Request.BookingRequest;
import com.shipping.booking_service.dto.Response.*;
import com.shipping.booking_service.entity.BookingStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request, Long userId);
    BookingResponse getById(Long bookingId, Long requesterId, boolean staff);
    PagedResponse<BookingResponse> listMyBookings(Long userId, BookingStatus status, Pageable pageable);
    PagedResponse<BookingResponse> listAll(BookingStatus status, Pageable pageable);
    BookingResponse cancelBooking(Long bookingId, Long requesterId);
    List<TimelineEventResponse> timeline(Long bookingId, Long requesterId, boolean staff);
    BookingSummaryResponse summary(Long userId, boolean staff);
    List<TrackedContainerResponse> trackedContainers(Long userId, boolean staff);
    boolean canTrack(Long userId, Long containerId, boolean staff);
    PagedResponse<BookingResponse> listForPort(Long portId, Pageable pageable);
    PagedResponse<BookingResponse> listForPort(Long portId, BookingStatus status, Pageable pageable);
    List<ContainerLoadResponse> containerLoad();
}