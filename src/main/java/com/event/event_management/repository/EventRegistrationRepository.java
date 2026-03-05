package com.event.event_management.repository;

import com.event.event_management.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    List<EventRegistration> findByStatus(String status);
    List<EventRegistration> findByEventId(Long eventId);
    java.util.Optional<EventRegistration> findByEventIdAndUserEmail(Long eventId, String userEmail);
}
