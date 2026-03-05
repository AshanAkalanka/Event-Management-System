package com.event.event_management.repository;

import com.event.event_management.entity.BookingService;
import com.event.event_management.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, Long> {
    List<BookingService> findByBooking(Booking booking);
    void deleteByBooking(Booking booking);
}
