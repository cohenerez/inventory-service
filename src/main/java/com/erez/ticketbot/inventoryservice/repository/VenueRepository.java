package com.erez.ticketbot.inventoryservice.repository;

import com.erez.ticketbot.inventoryservice.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue,Long> {
}
