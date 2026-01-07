package com.erez.ticketbot.inventoryservice.repository;

import com.erez.ticketbot.inventoryservice.entity.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, String> {

    Optional<Reservation> findByTransactionId(String transactionId);
    boolean existsByTransactionId(String transactionId);
    void deleteByTransactionId(String transactionId);
    List<Reservation> findByEventId(Long eventId);
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByEventIdAndStatus(Long eventId, Reservation.ReservationStatus status);
    List<Reservation> findByCreatedAtBefore(LocalDateTime dateTime);


      // Only fetches COMPENSATED or FAILED reservations that are old
    @Query("{ 'createdAt': { $lt: ?0 }, 'status': { $in: ?1 } }")
    List<Reservation> findOldTerminalReservations(LocalDateTime dateTime,List<Reservation.ReservationStatus> statuses);

    // status typically RESERVED
    @Query("{ 'createdAt': { $lt: ?0 }, 'status': ?1 }")
    List<Reservation> findStuckReservations(LocalDateTime dateTime, Reservation.ReservationStatus status);

    long countByStatus(Reservation.ReservationStatus status);
    long countByEventId(Long eventId);
    default long countActiveReservations() {
        return countByStatus(Reservation.ReservationStatus.RESERVED);
    }
    @Query("{ 'transactionId': { $in: ?0 } }")
    List<Reservation> findByTransactionIdIn(List<String> transactionIds);

    void deleteByStatus(Reservation.ReservationStatus status);

    @Query("{ 'status': 'FAILED', 'errorMessage': { $exists: true, $ne: null } }")
    List<Reservation> findFailedReservationsWithErrors();

    @Query("{ 'status': 'RESERVED', 'createdAt': { $lt: ?0 } }")
    List<Reservation> findReservationsNeedingReview(LocalDateTime daysOld);
}