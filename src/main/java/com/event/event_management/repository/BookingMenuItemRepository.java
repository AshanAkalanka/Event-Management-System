package com.event.event_management.repository;

import com.event.event_management.entity.BookingMenuItem;
import com.event.event_management.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingMenuItemRepository extends JpaRepository<BookingMenuItem, Long> {
    List<BookingMenuItem> findByBooking(Booking booking);
    void deleteByBooking(Booking booking);
}
