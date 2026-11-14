package com.shipping.booking_service.event;

public record PickupOtpEvent (Long bookingId, Long userId, String otp, int validMinutes, String purpose) {}
