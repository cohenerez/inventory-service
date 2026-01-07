package com.erez.ticketbot.inventoryservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    private String id;

    @Indexed(unique = true)
    private String transactionId;

    @Indexed
    private Long eventId;

    @Indexed
    private Long userId;

    private Long ticketCount;

    private Long originalCapacity;

    @Indexed
    private ReservationStatus status;

    @Indexed
    private LocalDateTime createdAt;

    private String errorMessage;
    public enum ReservationStatus {

        RESERVED,
        CONFIRMED,
        COMPENSATED,
        FAILED
    }


    public boolean canBeCompensated() {
        return this.status == ReservationStatus.RESERVED;
    }


    public boolean isTerminal() {
        return this.status == ReservationStatus.CONFIRMED ||
                this.status == ReservationStatus.COMPENSATED ||
                this.status == ReservationStatus.FAILED;
    }

    public long getAgeInHours() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toHours();
    }
}