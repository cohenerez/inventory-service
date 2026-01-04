package com.erez.ticketbot.inventoryservice.repository;

import com.erez.ticketbot.inventoryservice.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event,Long > {
}
