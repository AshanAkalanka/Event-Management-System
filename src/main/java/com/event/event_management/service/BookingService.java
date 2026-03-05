package com.event.event_management.service;

import com.event.event_management.entity.*;
import com.event.event_management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private BookingMenuItemRepository bookingMenuItemRepository;
    
    @Autowired
    private BookingServiceRepository bookingServiceRepository;
    
    @Autowired
    private MenuRepository menuRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private UserService userService;
    
    @Transactional
    public Booking createBooking(Long userId, Long eventId, int guestCount, 
                                 LocalDateTime eventDate, String venue, String venueType, String bookingType,
                                 String specialRequests, List<Long> menuIds, 
                                 List<Integer> menuQuantities, List<Long> serviceIds, 
                                 List<Integer> serviceQuantities) {
        
        User user = userService.getUserById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Event event = eventService.getEventById(eventId)
            .orElseThrow(() -> new RuntimeException("Event not found"));
        
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        booking.setGuestCount(guestCount);
        booking.setBookingDate(LocalDateTime.now());
        booking.setEventDate(eventDate);
        booking.setVenue(venue);
        booking.setVenueType(venueType);
        booking.setBookingType(bookingType);
        booking.setSpecialRequests(specialRequests);
        booking.setStatus("PENDING");
        
        BigDecimal mealPrice = BigDecimal.ZERO;
        BigDecimal servicePrice = BigDecimal.ZERO;
        
        // Calculate meal prices
        if (menuIds != null && menuQuantities != null) {
            for (int i = 0; i < menuIds.size() && i < menuQuantities.size(); i++) {
                Long menuId = menuIds.get(i);
                Integer quantity = menuQuantities.get(i);
                
                Optional<Menu> menuOpt = menuRepository.findById(menuId);
                if (menuOpt.isPresent() && menuOpt.get().getIsAvailable()) {
                    Menu menu = menuOpt.get();
                    BigDecimal unitPrice = menu.getPrice();
                    BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    mealPrice = mealPrice.add(itemTotal);
                    
                    BookingMenuItem bookingMenuItem = new BookingMenuItem();
                    bookingMenuItem.setBooking(booking);
                    bookingMenuItem.setMenu(menu);
                    bookingMenuItem.setQuantity(quantity);
                    bookingMenuItem.setUnitPrice(unitPrice);
                    bookingMenuItem.setTotalPrice(itemTotal);
                    bookingMenuItemRepository.save(bookingMenuItem);
                }
            }
        }
        
        // Calculate service prices
        if (serviceIds != null && serviceQuantities != null) {
            for (int i = 0; i < serviceIds.size() && i < serviceQuantities.size(); i++) {
                Long serviceId = serviceIds.get(i);
                Integer quantity = serviceQuantities.get(i);
                
                Optional<CateringService> serviceOpt = serviceRepository.findById(serviceId);
                if (serviceOpt.isPresent() && serviceOpt.get().getIsAvailable()) {
                    CateringService service = serviceOpt.get();
                    BigDecimal unitPrice = service.getPrice();
                    BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    servicePrice = servicePrice.add(itemTotal);
                    
                    com.event.event_management.entity.BookingService bookingServiceEntity = new com.event.event_management.entity.BookingService();
                    bookingServiceEntity.setBooking(booking);
                    bookingServiceEntity.setService(service);
                    bookingServiceEntity.setQuantity(quantity);
                    bookingServiceEntity.setUnitPrice(unitPrice);
                    bookingServiceEntity.setTotalPrice(itemTotal);
                    bookingServiceRepository.save(bookingServiceEntity);
                }
            }
        }
        
        booking.setMealPrice(mealPrice);
        booking.setServicePrice(servicePrice);
        booking.setTotalPrice(mealPrice.add(servicePrice));
        
        return bookingRepository.save(booking);
    }

    @Transactional
    public void approveBooking(Long id, String adminNote) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("APPROVED");
            booking.setReviewedAt(LocalDateTime.now());
            booking.setAdminNote(adminNote);
            bookingRepository.save(booking);
        }
    }

    @Transactional
    public void declineBooking(Long id, String adminNote) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("DECLINED");
            booking.setReviewedAt(LocalDateTime.now());
            booking.setAdminNote(adminNote);
            bookingRepository.save(booking);
        }
    }
    
    public List<Booking> getUserBookings(Long userId) {
        if (userId == null) {
            return new java.util.ArrayList<>();
        }
        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return bookingRepository.findByUserOrderByCreatedAtDesc(userOpt.get());
    }
    
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }
    
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
    
    @Transactional
    public void cancelBooking(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);
        }
    }
    
    @Transactional
    public void confirmBooking(Long id) {
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            booking.setStatus("CONFIRMED");
            bookingRepository.save(booking);
        }
    }
}
