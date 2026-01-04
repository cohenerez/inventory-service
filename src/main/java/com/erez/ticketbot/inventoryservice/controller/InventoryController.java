package com.erez.ticketbot.inventoryservice.controller;

import com.erez.ticketbot.inventoryservice.response.EventInventoryResponse;
import com.erez.ticketbot.inventoryservice.response.VenueInventoryResponse;
import com.erez.ticketbot.inventoryservice.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class InventoryController {

    private InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }


    @GetMapping("inventory/events")
    public @ResponseBody List<EventInventoryResponse> inventoryGetAllEvents() {
        return inventoryService.getAllEvents();

    }

    @GetMapping("inventory/event/{eventId}")
    public @ResponseBody EventInventoryResponse inventoryForEvent(@PathVariable("eventId") Long eventId) {
        return inventoryService.getEventInventory(eventId);

    }


    @GetMapping("/inventory/venue/{venueId}")
    public @ResponseBody VenueInventoryResponse inventoryByVenueId(@PathVariable("venueId") Long venueId) {
        return inventoryService.getVenueInformation(venueId);

    }

    @PutMapping("/inventory/event/{eventId}/capacity/{capacity}")
    public ResponseEntity<Void> updateEventCapacity(@PathVariable("eventId") Long eventId, @PathVariable("capacity") Long ticketsBooked){
    inventoryService.updateEventCapacity(eventId,ticketsBooked);
    return ResponseEntity.ok().build();

    }



}
