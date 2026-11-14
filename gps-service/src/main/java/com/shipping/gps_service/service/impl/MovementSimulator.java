package com.shipping.gps_service.service.impl;

import com.shipping.gps_service.dto.request.SimulateRequest;
import com.shipping.gps_service.event.LocationPingEvent;
import com.shipping.gps_service.event.LocationPingPublisher;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementSimulator {

    private static final BigDecimal SPEED_KMPH = BigDecimal.valueOf(40);

    private final LocationPingPublisher publisher;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final Map<Long, ScheduledFuture<?>> running = new ConcurrentHashMap<>();

    public void start(SimulateRequest r) {
        stop(r.containerId());

        int steps = r.steps() == null ? 60 : r.steps();
        long interval = r.intervalMs() == null ? 2000 : r.intervalMs();
        double lat1 = r.fromLat().doubleValue(), lng1 = r.fromLng().doubleValue();
        double lat2 = r.toLat().doubleValue(), lng2 = r.toLng().doubleValue();
        BigDecimal heading = BigDecimal.valueOf(bearing(lat1, lng1, lat2, lng2)).setScale(2, RoundingMode.HALF_UP);
        Long id = r.containerId();
        AtomicInteger n = new AtomicInteger();

        ScheduledFuture<?> f = scheduler.scheduleWithFixedDelay(() -> {
            try {
                int i = n.getAndIncrement();
                if (i > steps) {
                    ScheduledFuture<?> self = running.remove(id);
                    if (self != null) self.cancel(false);
                    log.info("Simulation finished for containerId={}", id);
                    return;
                }
                double t = (double) i / steps;
                publisher.publish(new LocationPingEvent(id,
                        scale(lat1 + (lat2 - lat1) * t),
                        scale(lng1 + (lng2 - lng1) * t),
                        i == steps ? BigDecimal.ZERO : SPEED_KMPH,
                        heading));
            } catch (Exception e) {
                log.error("Simulation tick failed for containerId={}", id, e);
            }
        }, interval, interval, TimeUnit.MILLISECONDS);

        running.put(id, f);
    }

    public void stop(Long containerId) {
        ScheduledFuture<?> f = running.remove(containerId);
        if (f != null) f.cancel(false);
    }

    public boolean isRunning(Long containerId) { return running.containsKey(containerId); }

    @PreDestroy
    void shutdown() { scheduler.shutdownNow(); }

    private static BigDecimal scale(double v) {
        return BigDecimal.valueOf(v).setScale(6, RoundingMode.HALF_UP);
    }

    private static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double p1 = Math.toRadians(lat1), p2 = Math.toRadians(lat2), dl = Math.toRadians(lon2 - lon1);
        double y = Math.sin(dl) * Math.cos(p2);
        double x = Math.cos(p1) * Math.sin(p2) - Math.sin(p1) * Math.cos(p2) * Math.cos(dl);
        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }
}
