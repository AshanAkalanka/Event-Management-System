package com.event.event_management.repository;

import com.event.event_management.entity.Update;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UpdateRepository extends JpaRepository<Update, Long> {
    
    @Modifying
    @Query("DELETE FROM Update u WHERE u.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}