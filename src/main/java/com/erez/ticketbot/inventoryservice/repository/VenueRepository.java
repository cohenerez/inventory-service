package com.erez.ticketbot.inventoryservice.repository;

import com.erez.ticketbot.inventoryservice.entity.Venue;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface VenueRepository extends MongoRepository<Venue, String> {

    Optional<Venue> findById(Long id);
    boolean existsById(Long id);
    Optional<Venue> findByName(String name);
    void deleteById(Long id);
}