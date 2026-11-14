package com.shipping.booking_service.service;

import com.shipping.booking_service.dto.Request.ReceiveRequest;
import com.shipping.booking_service.dto.Request.RejectGoodsRequest;
import com.shipping.booking_service.entity.*;
import com.shipping.booking_service.event.BookingEventPublisher;
import com.shipping.booking_service.event.outgoing.BookingStatusChangedEvent;
import com.shipping.booking_service.event.BookingTimeline;
import com.shipping.booking_service.event.PickupOtpEvent;
import com.shipping.booking_service.event.outgoing.BookingCancelledEvent;
import com.shipping.booking_service.exception.BookingNotFoundException;
import com.shipping.booking_service.exception.ContainerNotFoundException;
import com.shipping.booking_service.repository.BookedContainerRepository;
import com.shipping.booking_service.repository.ContainerRepository;
import com.shipping.booking_service.repository.PickupOtpRepository;
import com.shipping.booking_service.repository.PortInspectionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.shipping.booking_service.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PortDeskService {
    private static final int VALID_MIN = 10, MAX_ATTEMPTS = 5, COOLDOWN_SEC = 30;
    private static final BigDecimal TOLERANCE = new BigDecimal("0.05");

    private final BookedContainerRepository bookings;
    private final ContainerRepository containers;
    private final ContainerService containerService;
    private final PickupOtpRepository otps;
    private final PortInspectionRepository inspections;
    private final BookingTimeline timeline;
    private final BookingEventPublisher publisher;
    private final SecureRandom random = new SecureRandom();

    @Transactional()
    public Long portFor(Long id, boolean delivery) {
        BookedContainer b = find(id);
        return delivery ? b.getDestPortId() : b.getOriginPortId();
    }

    @Transactional
    public void sendOtp(Long id, boolean delivery) {
        BookedContainer b = find(id);
        if (delivery) require(b, BookingStatus.ARRIVED, "Goods have not arrived yet");
        else { require(b, BookingStatus.CONFIRMED, "Only paid goods waiting at the port"); requireDue(b); }
        PickupOtp row = otps.findById(id).orElseGet(PickupOtp::new);
        if (row.getCreatedAt() != null && row.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(COOLDOWN_SEC)))
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_COOLDOWN",
                    "Please wait " + COOLDOWN_SEC + " seconds before sending another code");
        String otp = String.format("%06d", random.nextInt(1_000_000));
        row.setBookingId(id);
        row.setOtpHash(hash(id, otp));
        row.setExpiresAt(LocalDateTime.now().plusMinutes(VALID_MIN));
        row.setAttempts(0);
        row.setCreatedAt(LocalDateTime.now());
        otps.save(row);
        Long userId = b.getUserId();
        String purpose = delivery ? "DELIVERY" : "HANDOVER";
        afterCommit(() -> publisher.publishPickupOtp(new PickupOtpEvent(id, userId, otp, VALID_MIN, purpose)));
    }

    /** accept: the inspection must pass AND the customer's code must match */
    @Transactional()
    public void receive(Long id, ReceiveRequest r, Long actor) {
        BookedContainer b = find(id);
        require(b, BookingStatus.CONFIRMED, "Only paid goods waiting at the port can be received");
        requireDue(b);
        checkInspection(b, r.packagingOk(), r.labelOk(), r.actualWeightKg());
        checkOtp(id, r.otp());
        saveInspection(id, true, r.packagingOk(), r.labelOk(), r.actualWeightKg(), r.note(), actor);
        b.setStatus(BookingStatus.AT_PORT);
        bookings.save(b);
        timeline.record(b, BookingStatus.AT_PORT, "Goods inspected and received at the port ("
                + r.actualWeightKg().stripTrailingZeros().toPlainString() + " kg)", actor);
    }

    @Transactional
    public void reject(Long id, RejectGoodsRequest r, Long actor) {
        BookedContainer b = find(id);
        require(b, BookingStatus.CONFIRMED, "Only goods waiting at the port can be rejected");
        Container c = containers.findByIdForUpdate(b.getContainer().getId())
                .orElseThrow(() -> new ContainerNotFoundException(b.getContainer().getId()));
        c.release(b.getTotalWeightKg());
        containers.save(c);
        containerService.evictCache(c.getId());
        saveInspection(id, false, r.packagingOk(), r.labelOk(), r.actualWeightKg(), r.reason(), actor);
        b.setStatus(BookingStatus.REJECTED);
        bookings.save(b);
        timeline.record(b, BookingStatus.REJECTED, "Rejected at port: " + r.reason(), actor);
        var changed = new BookingStatusChangedEvent(id, b.getUserId(), "CONFIRMED", "REJECTED", r.reason());
        var cancelled = new BookingCancelledEvent(id, b.getUserId(), c.getId());
        afterCommit(() -> { publisher.publishBookingCancelled(cancelled); publisher.publishBookingStatusChanged(changed); });
    }

    @Transactional()
    public void deliver(Long id, String otp, Long actor) {
        BookedContainer b = find(id);
        require(b, BookingStatus.ARRIVED, "Only arrived goods can be handed over");
        checkOtp(id, otp);
        b.setStatus(BookingStatus.DELIVERED);
        bookings.save(b);
        timeline.record(b, BookingStatus.DELIVERED, "Handed over to the customer (verified by SMS code)", actor);
        var changed = new BookingStatusChangedEvent(id, b.getUserId(), "ARRIVED", "DELIVERED", null);
        afterCommit(() -> publisher.publishBookingStatusChanged(changed));
        Container c = containers.findByIdForUpdate(b.getContainer().getId()).orElseThrow();
        if (bookings.findByContainer_IdAndStatus(c.getId(), BookingStatus.ARRIVED).isEmpty()) {
            c.setAllocatedKg(BigDecimal.ZERO);
            c.setStatus(Status.AVAILABLE);
            containers.save(c);
            containerService.evictCache(c.getId());
        }
    }

    // ---------------------------------------------------------------- helpers
    private void checkInspection(BookedContainer b, boolean pack, boolean label, BigDecimal actual) {
        if (!pack) throw new ApiException(HttpStatus.CONFLICT, "BAD_PACKAGING", "Packaging is not valid. Reject these goods.");
        if (!label) throw new ApiException(HttpStatus.CONFLICT, "BAD_LABEL", "Label or documents are not valid. Reject these goods.");
        BigDecimal declared = b.getTotalWeightKg();
        if (declared.signum() > 0 && actual.subtract(declared).abs()
                .divide(declared, 4, RoundingMode.HALF_UP).compareTo(TOLERANCE) > 0)
            throw new ApiException(HttpStatus.CONFLICT, "WEIGHT_MISMATCH",
                    "Actual weight " + actual.stripTrailingZeros().toPlainString() + " kg differs from the declared "
                            + declared.stripTrailingZeros().toPlainString() + " kg by more than 5%. Reject these goods.");
    }

    private void checkOtp(Long id, String code) {
        PickupOtp row = otps.findById(id).orElseThrow(() ->
                new ApiException(HttpStatus.CONFLICT, "NO_OTP", "Send a code to the customer first"));
        if (row.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new ApiException(HttpStatus.GONE, "OTP_EXPIRED", "The code has expired. Send a new one.");
        if (row.getAttempts() >= MAX_ATTEMPTS)
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "OTP_LOCKED", "Too many wrong codes. Send a new one.");
        if (!MessageDigest.isEqual(hash(id, code).getBytes(StandardCharsets.UTF_8),
                row.getOtpHash().getBytes(StandardCharsets.UTF_8))) {
            row.setAttempts(row.getAttempts() + 1);
            otps.save(row);
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP_INVALID", "Wrong code");
        }
        otps.delete(row);
    }

    private void saveInspection(Long id, boolean ok, boolean pack, boolean label, BigDecimal w, String note, Long actor) {
        PortInspection p = new PortInspection();
        p.setBookingId(id); p.setAccepted(ok); p.setPackagingOk(pack); p.setLabelOk(label);
        p.setActualWeightKg(w); p.setNote(note); p.setInspectedBy(actor);
        inspections.save(p);
    }

    private BookedContainer find(Long id) {
        return bookings.findById(id).orElseThrow(() -> new BookingNotFoundException(id));
    }
    private void require(BookedContainer b, BookingStatus s, String msg) {
        if (b.getStatus() != s)
            throw new ApiException(HttpStatus.CONFLICT, "WRONG_STATUS", msg + " (current: " + b.getStatus() + ")");
    }
    private void requireDue(BookedContainer b) {
        if (b.getScheduleAt().toLocalDate().isAfter(LocalDate.now()))
            throw new ApiException(HttpStatus.CONFLICT, "NOT_SCHEDULED_YET",
                    "This booking departs on " + b.getScheduleAt() + ". Goods are received from that day.");
    }
    private static String hash(Long id, String otp) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest((id + ":" + otp).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : d) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static void afterCommit(Runnable r) {
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { r.run(); }
            });
        else r.run();
    }
}