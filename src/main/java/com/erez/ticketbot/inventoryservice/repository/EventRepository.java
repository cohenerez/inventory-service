package com.erez.ticketbot.inventoryservice.repository;

import com.erez.ticketbot.inventoryservice.entity.Event;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB Repository for Event documents
 * Replaces JPA EventRepository with MongoDB operations
 */
@Repository
public interface EventRepository extends MongoRepository<Event, String> {


    Optional<Event> findById(Long id);
    boolean existsById(Long id);
    Optional<Event> findByName(String name);
    void deleteById(Long id);
}