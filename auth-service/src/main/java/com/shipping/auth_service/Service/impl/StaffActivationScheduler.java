package com.shipping.auth_service.Service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StaffActivationScheduler {
    private final CareerService careers;

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Kolkata")   // every day 00:05 IST
    public void daily() { run(); }

    @EventListener(ApplicationReadyEvent.class)                // catch-up if the server was down at 00:05
    public void onStartup() { run(); }

    private void run() {
        try {
            int n = careers.activateDueStaff();
            if (n > 0) log.info("Activated {} staff account(s)", n);
        } catch (Exception e) {
            log.error("Staff activation failed", e);
        }
    }
}