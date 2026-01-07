package com.erez.ticketbot.inventoryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    private String mongoId;

    @Indexed(unique = true)
    private Long id;

    @Indexed
    private String name;

    private Long totalCapacity;

    @Indexed
    private Long leftCapacity;

    private BigDecimal ticketPrice;

    private Venue venue;
}