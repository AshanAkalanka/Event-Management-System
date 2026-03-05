package com.event.event_management.service;

import com.event.event_management.entity.EventRegistration;
import com.event.event_management.repository.EventRegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EventRegistrationService {

    @Autowired
    private EventRegistrationRepository repository;

    public EventRegistration save(EventRegistration registration) {
        return repository.save(registration);
    }

    public List<EventRegistration> getAll() {
        return repository.findAll();
    }

    public List<EventRegistration> getPending() {
        return repository.findByStatus("PENDING");
    }

    public Optional<EventRegistration> getById(Long id) {
        return repository.findById(id);
    }

    public Optional<EventRegistration> findByEventIdAndUserEmail(Long eventId, String userEmail) {
        return repository.findByEventIdAndUserEmail(eventId, userEmail);
    }

    public void approve(Long id) {
        repository.findById(id).ifPresent(reg -> {
            reg.setStatus("APPROVED");
            repository.save(reg);
        });
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
