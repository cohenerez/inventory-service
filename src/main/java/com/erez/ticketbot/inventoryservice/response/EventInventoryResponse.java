package com.erez.ticketbot.inventoryservice.response;

import com.erez.ticketbot.inventoryservice.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInventoryResponse {
    private Long eventId;
    private String event;
    private Long capacity;
    private Venue venue;
    private BigDecimal ticketPrice;

}
