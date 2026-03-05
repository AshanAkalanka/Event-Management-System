package com.event.event_management.repository;

import com.event.event_management.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    java.util.Optional<Event> findByName(String name);
}