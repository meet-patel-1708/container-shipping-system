package com.shipping.auth_service.Config;

import com.shipping.auth_service.Service.impl.ResignationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResignationJob {
    private final ResignationService service;

    @Scheduled(cron = "0 5 * * * *")   // five past every hour
    public void run() { service.completeDue(); }
}