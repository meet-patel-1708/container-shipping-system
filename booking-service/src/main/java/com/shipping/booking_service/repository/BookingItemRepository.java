package com.shipping.booking_service.repository;

import com.shipping.booking_service.entity.BookedContainer;
import com.shipping.booking_service.entity.BookingItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingItemRepository extends JpaRepository<BookingItem, Long> {
    List<BookingItem> findByBooking_Id(Long bookingId);
}
