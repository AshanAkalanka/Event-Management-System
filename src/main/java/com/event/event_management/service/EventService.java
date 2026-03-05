package com.event.event_management.service;

import com.event.event_management.entity.Event;
import com.event.event_management.repository.EventRepository;
import com.event.event_management.repository.ResourceRepository;
import com.event.event_management.repository.UpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;     // Repository to interact with the database
    
    @Autowired
    private ResourceRepository resourceRepository;  // Repository to interact with Resource table
    
    @Autowired
    private UpdateRepository updateRepository;      // Repository to interact with Update table

    // Get all events from the database
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // Find a specific event by its ID
    public Optional<Event> getEventById(Long id) {
        Optional<Event> eventOpt = eventRepository.findById(id);
        if (eventOpt.isPresent()) {
            Event event = eventOpt.get();
            // Force loading of lazy relationships
            if (event.getResources() != null) {
                event.getResources().size();
            }
            if (event.getUpdates() != null) {
                event.getUpdates().size();
            }
        }
        return eventOpt;
    }

    // Find a specific event by its name
    public Optional<Event> getEventByName(String name) {
        return eventRepository.findByName(name);
    }


    // Save, Update, Delete event
    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        // First delete all linked resources
        resourceRepository.deleteByEventId(id);
        
        // Then delete all linked updates
        updateRepository.deleteByEventId(id);
        
        // Finally delete the event itself
        eventRepository.deleteById(id);
    }

    // Mark an event as "Completed"
    public void markCompleted(Long id) {
        Optional<Event> eventOpt = getEventById(id);
        eventOpt.ifPresent(event -> {
            event.setStatus("Completed");
            saveEvent(event);
        });
    }

    // Fix existing events that don't have a status
    public void fixEventStatus() {
        List<Event> events = getAllEvents();
        System.out.println("=== Fixing Event Status ===");
        for (Event event : events) {
            System.out.println("Event ID: " + event.getId() + ", Current Status: " + event.getStatus());
            if (event.getStatus() == null || event.getStatus().trim().isEmpty()) {
                event.setStatus("Planned");
                saveEvent(event);
                System.out.println("Fixed Event ID: " + event.getId() + ", New Status: " + event.getStatus());
            }
        }
        System.out.println("=== Status Fix Complete ===");
    }

    // Get the newest event (most recently created)
    public Optional<Event> getNewestEvent() {
        List<Event> events = getAllEvents();
        return events.stream()
                .max((e1, e2) -> e1.getId().compareTo(e2.getId()));
    }

    // Change NEW status to Planned after some time (optional)
    public void markNewAsPlanned(Long id) {
        Optional<Event> eventOpt = getEventById(id);
        eventOpt.ifPresent(event -> {
            if ("NEW".equals(event.getStatus())) {
                event.setStatus("Planned");
                saveEvent(event);
            }
        });
    }

    // Change all existing NEW events to Planned (so only one NEW event exists)
    public void changeAllNewToPlanned() {
        List<Event> events = getAllEvents();
        System.out.println("=== Changing all NEW events to Planned ===");
        for (Event event : events) {
            if ("NEW".equals(event.getStatus())) {
                event.setStatus("Planned");
                saveEvent(event);
                System.out.println("Changed Event ID: " + event.getId() + " from NEW to Planned");
            }
        }
        System.out.println("=== All NEW events changed to Planned ===");
    }
}