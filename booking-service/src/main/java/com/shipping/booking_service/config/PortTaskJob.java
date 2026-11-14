package com.shipping.booking_service.config;

import com.shipping.booking_service.entity.BookedContainer;
import com.shipping.booking_service.entity.BookingStatus;
import com.shipping.booking_service.event.BookingEventPublisher;
import com.shipping.booking_service.event.incoming.PortEvent.PortTaskEvent;
import com.shipping.booking_service.repository.BookedContainerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PortTaskJob {
    private final BookedContainerRepository bookings;
    private final BookingEventPublisher publisher;

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void notifyDue() {
        LocalDateTime cutoff = LocalDate.now().plusDays(1).atStartOfDay();   // departs today or earlier
        for (BookedContainer b : bookings.findByStatusAndScheduleAtBeforeAndPortNotifiedFalse(
                BookingStatus.CONFIRMED, cutoff)) {
            b.setPortNotified(true);
            bookings.save(b);
            var e = new PortTaskEvent(b.getId(), b.getContainer().getId(), b.getOriginPortId(),
                    b.getScheduleAt().toString(), b.getTotalWeightKg());
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { publisher.publishPortTask(e); }
            });
        }
    }
}
