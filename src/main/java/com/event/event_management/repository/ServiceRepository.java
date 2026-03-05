package com.event.event_management.repository;

import com.event.event_management.entity.CateringService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<CateringService, Long> {
    List<CateringService> findByIsAvailableTrue();
}
