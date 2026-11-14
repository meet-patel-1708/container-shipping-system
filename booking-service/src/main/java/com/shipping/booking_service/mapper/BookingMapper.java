package com.shipping.booking_service.mapper;

import com.shipping.booking_service.dto.Response.BookingItemResponse;
import com.shipping.booking_service.dto.Response.BookingResponse;
import com.shipping.booking_service.entity.BookedContainer;
import com.shipping.booking_service.entity.BookingItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(BookedContainer booking) {
        List<BookingItemResponse> itemResponses = booking.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal total = itemResponses.stream().map(BookingItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BookingResponse(booking.getId(), booking.getContainer().getId(), booking.getUserId(),
                booking.getShipId(), booking.getOriginPortId(), booking.getDestPortId(), booking.getStatus(),
                total, itemResponses, booking.getCreatedAt(), booking.getUpdatedAt(), booking.getTotalWeightKg(),booking.getScheduleAt(),booking.getContainer().remainingKg());
    }

    public BookingItemResponse toItemResponse(BookingItem item) {
        BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new BookingItemResponse(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                subtotal,
                item.getProductName()
        );
    }
}