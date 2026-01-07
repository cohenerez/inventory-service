package com.erez.ticketbot.inventoryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "venues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    private String mongoId;

    @Indexed(unique = true)
    private Long id;

    @Indexed
    private String name;

    private String address;

    private Long totalCapacity;
}