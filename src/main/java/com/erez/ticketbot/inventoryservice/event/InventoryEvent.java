package com.erez.ticketbot.inventoryservice.event;

import java.math.BigDecimal;

public record InventoryEvent(
        String transactionId,
        Long userId,
        Long eventId,
        Long ticketCount,
        BigDecimal totalPrice,
        EventType eventType,
        String errorMessage
) {

    public InventoryEvent {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("Transaction ID cannot be null or empty");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("Event type cannot be null");
        }
    }

    public InventoryEvent(String transactionId, Long userId, Long eventId,
                          Long ticketCount, BigDecimal totalPrice, EventType eventType) {
        this(transactionId, userId, eventId, ticketCount, totalPrice, eventType, null);
    }


    public static InventoryEvent failure(String transactionId, Long userId, Long eventId,
                                         Long ticketCount, EventType eventType, String errorMessage) {
        return new InventoryEvent(transactionId, userId, eventId, ticketCount, null, eventType, errorMessage);
    }


    public static InventoryEvent compensation(String transactionId, EventType eventType) {
        return new InventoryEvent(transactionId, null, null, null, null, eventType, null);
    }


    public enum EventType {
        BOOKING_VALIDATED,
        INVENTORY_RESERVED,
        INVENTORY_RESERVATION_FAILED,
        COMPENSATE_INVENTORY,
        INVENTORY_COMPENSATED
    }
}
