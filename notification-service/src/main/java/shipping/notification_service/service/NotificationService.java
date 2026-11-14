package shipping.notification_service.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shipping.notification_service.dto.response.NotificationResponse;
import shipping.notification_service.dto.response.PagedResponse;

public interface NotificationService {
    void createAndSend(Long userId, Long bookingId, String title, String message);
    void sendOtp(Long userId, Long bookingId, String otp, int validMinutes, String purpose);
    NotificationResponse getById(Long id, Long requesterId);
    PagedResponse<NotificationResponse> listByUser(Long userId, Pageable pageable);

    @Slf4j
    @Service
    class SmsService {
        @Value("${twilio.enabled:false}") private boolean enabled;
        @Value("${twilio.account-sid:}") private String sid;
        @Value("${twilio.auth-token:}") private String token;
        @Value("${twilio.from:}") private String from;

        @PostConstruct
        void init() { if (enabled) Twilio.init(sid, token); }

        public boolean send(String phone, String body) {
            if (!enabled || phone == null || phone.isBlank()) return false;
            String to = phone.startsWith("+") ? phone : "+91" + phone;
            try {
                Message.creator(new PhoneNumber(to), new PhoneNumber(from), body).create();
                return true;
            } catch (Exception e) {
                log.error("SMS failed for {}", to, e);   // never log the message: it holds the code
                return false;
            }
        }
    }
}
