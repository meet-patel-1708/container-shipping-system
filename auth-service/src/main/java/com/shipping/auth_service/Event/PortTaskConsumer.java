package com.shipping.auth_service.Event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shipping.auth_service.Repository.UserRepository;
import com.shipping.auth_service.Service.EmailSenderService;
import com.shipping.auth_service.entity.Role;
import com.shipping.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortTaskConsumer {
    record PortTask(Long bookingId, Long containerId, Long originPortId, String scheduleDate, BigDecimal weightKg) {}

    private final UserRepository users;
    private final EmailSenderService mail;
    private final ObjectMapper mapper;

    @KafkaListener(topics = "port-task-events", groupId = "${spring.kafka.consumer.group-id}")
    public void onTask(String payload) {
        try {
            PortTask t = mapper.readValue(payload, PortTask.class);
            for (User u : users.findByRoleAndAssignedPortId(Role.EMPLOYEE, t.originPortId())) {
                try {
                    mail.sendPortTaskEmail(u.getEmail(), u.getName(), t.bookingId(), t.containerId(),
                            t.scheduleDate(), String.valueOf(t.weightKg()));
                } catch (Exception e) { log.error("Port task email failed for {}", u.getEmail(), e); }
            }
        } catch (Exception e) { log.error("FAILED port-task event {}", payload, e); }
    }
}
