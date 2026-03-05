package com.event.event_management.repository;

import com.event.event_management.entity.Booking;
import com.event.event_management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser(User user);
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    List<Booking> findByStatus(String status);
}
