package com.erez.ticketbot.inventoryservice.service;

import com.erez.ticketbot.inventoryservice.entity.Event;
import com.erez.ticketbot.inventoryservice.entity.Reservation;
import com.erez.ticketbot.inventoryservice.entity.Venue;
import com.erez.ticketbot.inventoryservice.event.InventoryEvent;
import com.erez.ticketbot.inventoryservice.repository.EventRepository;
import com.erez.ticketbot.inventoryservice.repository.ReservationRepository;
import com.erez.ticketbot.inventoryservice.repository.VenueRepository;
import com.erez.ticketbot.inventoryservice.response.EventInventoryResponse;
import com.erez.ticketbot.inventoryservice.response.VenueInventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class InventoryService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ReservationRepository reservationRepository;
    private final KafkaTemplate<String, InventoryEvent> kafkaTemplate;

    @Autowired
    public InventoryService(EventRepository eventRepository,
                            VenueRepository venueRepository,
                            ReservationRepository reservationRepository,
                            KafkaTemplate<String, InventoryEvent> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.reservationRepository = reservationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }


    public List<EventInventoryResponse> getAllEvents() {
        final List<Event> events = this.eventRepository.findAll();

        return events.stream().map(event -> EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getLeftCapacity())
                .venue(event.getVenue())
                .build()).collect(Collectors.toList());
    }


    public VenueInventoryResponse getVenueInformation(Long venueId) {
        final Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found: " + venueId));

        return VenueInventoryResponse.builder()
                .venueId(venue.getId())
                .venueName(venue.getName())
                .totalCapacity(venue.getTotalCapacity())
                .build();
    }


    public EventInventoryResponse getEventInventory(Long eventId) {
        final Event event = this.eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        return EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getLeftCapacity())
                .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                .eventId(event.getId())
                .build();
    }


    @Transactional
    public void updateEventCapacity(final Long eventId, final Long ticketsBooked) {
        final Event event = this.eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found: " + eventId));

        event.setLeftCapacity(event.getLeftCapacity() - ticketsBooked);
        eventRepository.save(event);  // MongoDB: save() is sufficient

        log.info("Event capacity updated. Event ID: {}, New left capacity: {}",
                eventId, event.getLeftCapacity());
    }


    @KafkaListener(topics = "booking-events", groupId = "inventory-service")
    public void handleBookingEvent(InventoryEvent event) {
        if (event.eventType() == InventoryEvent.EventType.BOOKING_VALIDATED) {
            reserveInventory(event);
        }
    }

    @Transactional
    public void reserveInventory(InventoryEvent bookingEvent) {
        log.info("Reserving inventory for transaction: {}", bookingEvent.transactionId());

        try {
            // Idempotency check: prevent duplicate reservations
            if (reservationRepository.existsByTransactionId(bookingEvent.transactionId())) {
                log.warn("Reservation already exists for transaction: {}, skipping",
                        bookingEvent.transactionId());
                return;
            }

            // Find event and validate capacity
            Event event = eventRepository.findById(bookingEvent.eventId())
                    .orElseThrow(() -> new RuntimeException("Event not found: " + bookingEvent.eventId()));

            if (event.getLeftCapacity() < bookingEvent.ticketCount()) {
                throw new RuntimeException(
                        String.format("Insufficient capacity. Available: %d, Requested: %d",
                                event.getLeftCapacity(), bookingEvent.ticketCount())
                );
            }

            // Store original capacity for compensation
            Long originalCapacity = event.getLeftCapacity();

            // Reserve inventory - update event capacity
            event.setLeftCapacity(event.getLeftCapacity() - bookingEvent.ticketCount());
            eventRepository.save(event);

            // PRODUCTION: Persist reservation to MongoDB for compensation tracking
            Reservation reservation = Reservation.builder()
                    .transactionId(bookingEvent.transactionId())
                    .eventId(bookingEvent.eventId())
                    .userId(bookingEvent.userId())
                    .ticketCount(bookingEvent.ticketCount())
                    .originalCapacity(originalCapacity)
                    .status(Reservation.ReservationStatus.RESERVED)
                    .createdAt(LocalDateTime.now())
                    .build();

            reservationRepository.save(reservation);

            log.info("Reserved {} tickets for event {}. New capacity: {}. Reservation persisted to MongoDB.",
                    bookingEvent.ticketCount(), bookingEvent.eventId(), event.getLeftCapacity());

            // Publish INVENTORY_RESERVED event for OrderService
            InventoryEvent inventoryReserved = new InventoryEvent(
                    bookingEvent.transactionId(),
                    bookingEvent.userId(),
                    bookingEvent.eventId(),
                    bookingEvent.ticketCount(),
                    bookingEvent.totalPrice(),
                    InventoryEvent.EventType.INVENTORY_RESERVED
            );

            kafkaTemplate.send("inventory-events", inventoryReserved);
            log.info("INVENTORY_RESERVED event published for transaction: {}", bookingEvent.transactionId());

        } catch (Exception e) {
            log.error("Inventory reservation failed for transaction: {}", bookingEvent.transactionId(), e);

            // Save failed reservation for audit trail
            try {
                Reservation failedReservation = Reservation.builder()
                        .transactionId(bookingEvent.transactionId())
                        .eventId(bookingEvent.eventId())
                        .userId(bookingEvent.userId())
                        .ticketCount(bookingEvent.ticketCount())
                        .status(Reservation.ReservationStatus.FAILED)
                        .errorMessage(e.getMessage())
                        .createdAt(LocalDateTime.now())
                        .build();

                reservationRepository.save(failedReservation);
            } catch (Exception saveEx) {
                log.error("Failed to save error reservation", saveEx);
            }

            // Publish failure event to trigger compensation
            InventoryEvent failureEvent = InventoryEvent.failure(
                    bookingEvent.transactionId(),
                    bookingEvent.userId(),
                    bookingEvent.eventId(),
                    bookingEvent.ticketCount(),
                    InventoryEvent.EventType.INVENTORY_RESERVATION_FAILED,
                    e.getMessage()
            );

            kafkaTemplate.send("inventory-events", failureEvent);
        }
    }

    /**
     * SAGA Pattern: Listen for compensation events
     */
    @KafkaListener(topics = "inventory-events", groupId = "inventory-compensation")
    public void handleCompensation(InventoryEvent event) {
        if (event.eventType() == InventoryEvent.EventType.COMPENSATE_INVENTORY) {
            compensateInventory(event);
        }
    }


    @Transactional
    public void compensateInventory(InventoryEvent compensationEvent) {
        log.info("Compensating inventory for transaction: {}", compensationEvent.transactionId());

        try {
            // PRODUCTION: Retrieve reservation from MongoDB (survives restarts!)
            Reservation reservation = reservationRepository
                    .findByTransactionId(compensationEvent.transactionId())
                    .orElse(null);

            if (reservation == null) {
                log.warn("No reservation found in MongoDB for transaction: {}",
                        compensationEvent.transactionId());
                return;
            }

            // Only compensate if reservation is in RESERVED state
            if (reservation.getStatus() != Reservation.ReservationStatus.RESERVED) {
                log.warn("Reservation already processed with status: {} for transaction: {}",
                        reservation.getStatus(), compensationEvent.transactionId());
                return;
            }

            // Find event and restore inventory
            Event event = eventRepository.findById(reservation.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event not found: " + reservation.getEventId()));

            // Restore inventory to pre-reservation capacity
            event.setLeftCapacity(event.getLeftCapacity() + reservation.getTicketCount());
            eventRepository.save(event);

            log.info("Restored {} tickets for event {}. New capacity: {}",
                    reservation.getTicketCount(), reservation.getEventId(), event.getLeftCapacity());

            // Update reservation status to COMPENSATED
            reservation.setStatus(Reservation.ReservationStatus.COMPENSATED);
            reservationRepository.save(reservation);

            log.info("Reservation marked as COMPENSATED in MongoDB for transaction: {}",
                    compensationEvent.transactionId());

            // Publish compensation success event
            InventoryEvent compensated = new InventoryEvent(
                    compensationEvent.transactionId(),
                    null,
                    null,
                    null,
                    null,
                    InventoryEvent.EventType.INVENTORY_COMPENSATED
            );

            kafkaTemplate.send("inventory-events", compensated);
            log.info("INVENTORY_COMPENSATED event published for transaction: {}",
                    compensationEvent.transactionId());

        } catch (Exception e) {
            log.error("Inventory compensation failed for transaction: {}",
                    compensationEvent.transactionId(), e);

            // Update reservation with error message
            try {
                reservationRepository.findByTransactionId(compensationEvent.transactionId())
                        .ifPresent(reservation -> {
                            reservation.setErrorMessage(e.getMessage());
                            reservationRepository.save(reservation);
                        });
            } catch (Exception saveEx) {
                log.error("Failed to save compensation error", saveEx);
            }
        }
    }

    @Transactional
    public void cleanupOldReservations(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<Reservation> oldReservations = reservationRepository.findByCreatedAtBefore(cutoffDate);

        int cleanedCount = 0;
        for (Reservation reservation : oldReservations) {
            if (reservation.getStatus() == Reservation.ReservationStatus.COMPENSATED ||
                    reservation.getStatus() == Reservation.ReservationStatus.FAILED) {
                reservationRepository.delete(reservation);
                cleanedCount++;
                log.debug("Cleaned up old reservation: {}", reservation.getTransactionId());
            }
        }

        if (cleanedCount > 0) {
            log.info("Cleanup completed: {} old reservations removed", cleanedCount);
        }
    }
}