package com.erez.ticketbot.inventoryservice.service;

import com.erez.ticketbot.inventoryservice.entity.Event;
import com.erez.ticketbot.inventoryservice.entity.Venue;
import com.erez.ticketbot.inventoryservice.repository.EventRepository;
import com.erez.ticketbot.inventoryservice.response.EventInventoryResponse;
import com.erez.ticketbot.inventoryservice.repository.VenueRepository;
import com.erez.ticketbot.inventoryservice.response.VenueInventoryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InventoryService {

    private EventRepository eventRepository;
    private VenueRepository venueRepository;

    @Autowired
   public InventoryService(EventRepository eventRepository,VenueRepository venueRepository){
       this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
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
      final Venue venueTemp = venueRepository.findById(venueId).orElse(null);

      return VenueInventoryResponse.builder()
              .venueId((venueTemp.getId()))
              .venueName(venueTemp.getName())
              .totalCapacity(venueTemp.getTotalCapacity())
              .build();
    }

    public EventInventoryResponse getEventInventory(Long eventId) {
        final Event event = this.eventRepository.findById(eventId).orElse(null);
        return EventInventoryResponse.builder()
                .event(event.getName())
                .capacity(event.getLeftCapacity())
                .venue(event.getVenue())
                .ticketPrice(event.getTicketPrice())
                .eventId(event.getId())
                .build();

     }

    public void updateEventCapacity(final Long eventId,final Long ticketsBooked) {
        final Event event = this.eventRepository.findById(eventId).orElse(null);
        event.setLeftCapacity(event.getLeftCapacity() - ticketsBooked);
        eventRepository.saveAndFlush(event);
       log.info("Event capacity updated new left capacity is :  {}",event.getLeftCapacity());
    }
}
