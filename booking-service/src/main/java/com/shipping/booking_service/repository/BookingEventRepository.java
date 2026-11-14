package com.shipping.booking_service.repository;

import com.shipping.booking_service.entity.BookingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingEventRepository extends JpaRepository<BookingEvent, Long> {
    List<BookingEvent> findByBooking_IdOrderByCreatedAtAsc(Long bookingId);
}
