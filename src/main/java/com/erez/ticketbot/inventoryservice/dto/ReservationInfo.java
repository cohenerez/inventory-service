package com.erez.ticketbot.inventoryservice.dto;

@lombok.Data
@lombok.AllArgsConstructor
public class ReservationInfo {

    private Long eventId;
    private Long ticketCount;
    private Long originalCapacity;
}
