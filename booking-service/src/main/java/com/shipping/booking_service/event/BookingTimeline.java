package com.shipping.booking_service.event;

import com.shipping.booking_service.entity.BookedContainer;
import com.shipping.booking_service.entity.BookingEvent;
import com.shipping.booking_service.entity.BookingStatus;
import com.shipping.booking_service.repository.BookingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingTimeline {
    private final BookingEventRepository repository;

    public void record(BookedContainer booking, BookingStatus status, String note, Long actorId) {
        BookingEvent e = new BookingEvent();
        e.setBooking(booking);
        e.setStatus(status);
        e.setNote(note);
        e.setCreatedBy(actorId);
        repository.save(e);
    }
}
