package shipping.notification_service.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import shipping.notification_service.dto.response.NotificationResponse;
import shipping.notification_service.dto.response.PagedResponse;
import shipping.notification_service.entity.Channel;
import shipping.notification_service.entity.Notification;
import shipping.notification_service.entity.NotificationStatus;
import shipping.notification_service.entity.UserContact;
import shipping.notification_service.exception.NotificationNotFoundException;
import shipping.notification_service.mapper.NotificationMapper;
import shipping.notification_service.repository.NotificationRepository;
import shipping.notification_service.repository.UserContactRepository;
import shipping.notification_service.service.NotificationService;

import java.time.LocalDateTime;
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository repository;
    private final UserContactRepository contacts;
    private final JavaMailSender mailSender;
    private final NotificationMapper mapper;
    private final SmsService smsService;
    @Override
    public void createAndSend(Long userId, Long bookingId, String title, String message) {
        deliver(userId, bookingId, title, message, message);
    }

    @Override
    public void sendOtp(Long userId, Long bookingId, String otp, int validMinutes, String purpose) {
        String what = "DELIVERY".equals(purpose) ? "receive your goods at the destination port"
                : "hand over your goods at the port";
        String text = "Container Shipping: your code to " + what + " (booking #" + bookingId + ") is " + otp
                + ". Valid " + validMinutes + " min. Share it only with port staff.";
        Notification n = new Notification();
        n.setUserId(userId); n.setBookingId(bookingId); n.setChannel(Channel.SMS);
        n.setTitle("Verification code");
        n.setMessage("A verification code was sent for booking #" + bookingId + ".");
        n.setStatus(NotificationStatus.QUEUED);
        n = repository.save(n);

        UserContact c = contacts.findById(userId).orElse(null);
        boolean sent = c != null && smsService.send(c.getPhone(), text);
        if (!sent && c != null) {
            try {
                SimpleMailMessage m = new SimpleMailMessage();
                m.setTo(c.getEmail()); m.setSubject("Verification code - Container Shipping System");
                m.setText("Hello " + c.getName() + ",\n\n" + text);
                mailSender.send(m);
                n.setChannel(Channel.EMAIL);
                sent = true;
            } catch (Exception e) { log.error("Could not email code to userId={}", userId, e); }
        }
        n.setStatus(sent ? NotificationStatus.SENT : NotificationStatus.FAILED);
        if (sent) n.setSentAt(LocalDateTime.now());
        repository.save(n);
    }

    private void deliver(Long userId, Long bookingId, String title, String stored, String emailBody) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setBookingId(bookingId);
        n.setChannel(Channel.EMAIL);
        n.setTitle(title);
        n.setMessage(stored.length() > 500 ? stored.substring(0, 500) : stored);
        n.setStatus(NotificationStatus.QUEUED);
        n = repository.save(n);

        UserContact c = contacts.findById(userId).orElse(null);
        if (c == null) {
            log.warn("No email on file for userId={}", userId);
            n.setStatus(NotificationStatus.FAILED);
            repository.save(n);
            return;
        }
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setTo(c.getEmail());
            m.setSubject(title + " - Container Shipping System");
            m.setText("Hello " + c.getName() + ",\n\n" + emailBody + "\n\nRegards,\nContainer Shipping System");
            mailSender.send(m);
            n.setStatus(NotificationStatus.SENT);
            n.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Could not email userId={}", userId, e);
            n.setStatus(NotificationStatus.FAILED);
        }
        repository.save(n);
    }

    @Override
    public NotificationResponse getById(Long id, Long requesterId) {
        Notification n = repository.findById(id)
                .filter(x -> x.getUserId().equals(requesterId))
                .orElseThrow(() -> new NotificationNotFoundException(id));
        return mapper.toResponse(n);
    }

    @Override
    public PagedResponse<NotificationResponse> listByUser(Long userId, Pageable pageable) {
        return PagedResponse.from(repository.findByUserId(userId, pageable).map(mapper::toResponse));
    }
}