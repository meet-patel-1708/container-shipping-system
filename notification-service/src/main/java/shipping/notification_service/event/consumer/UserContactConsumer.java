package shipping.notification_service.event.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shipping.notification_service.entity.UserContact;
import shipping.notification_service.repository.UserContactRepository;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserContactConsumer {
    record UserEvent(Long userId, String name, String email, String phone) {}

    private final UserContactRepository repository;
    private final ObjectMapper mapper;

    @KafkaListener(topics = {"user-registered-events", "user-updated-events"},
            groupId = "${spring.kafka.consumer.group-id}")
    public void onUser(String payload) {
        try {
            UserEvent e = mapper.readValue(payload, UserEvent.class);
            if (e.userId() == null || e.email() == null) return;
            UserContact c = repository.findById(e.userId()).orElseGet(UserContact::new);
            c.setUserId(e.userId()); c.setName(e.name()); c.setEmail(e.email());
            if (e.phone() != null) c.setPhone(e.phone());
            c.setUpdatedAt(LocalDateTime.now());
            repository.save(c);
        } catch (Exception ex) {
            log.error("FAILED TO PROCESS user event. payload={}", payload, ex);
        }
    }
}
