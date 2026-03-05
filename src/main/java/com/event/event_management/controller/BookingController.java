package com.event.event_management.controller;

import com.event.event_management.entity.Booking;
import com.event.event_management.entity.Event;
import com.event.event_management.entity.Menu;
import com.event.event_management.entity.CateringService;
import com.event.event_management.service.BookingService;
import com.event.event_management.service.EventService;
import com.event.event_management.service.MenuService;
import com.event.event_management.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private ServiceService serviceService;
    
    @GetMapping("/customer/book-event")
    public String bookEventForm(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        List<Event> events = eventService.getAllEvents();
        List<Menu> menus = menuService.getAvailableMenus();
        List<CateringService> services = serviceService.getAvailableServices();
        
        model.addAttribute("events", events);
        model.addAttribute("menus", menus);
        model.addAttribute("services", services);
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        
        return "customer/book-event";
    }
    
    @PostMapping("/customer/book-event")
    @ResponseBody
    public Map<String, Object> createBooking(@RequestParam String eventName,
                                           @RequestParam int guestCount,
                                           @RequestParam String eventDate,
                                           @RequestParam String venue,
                                           @RequestParam(required = false) String venueType,
                                           @RequestParam String bookingType,
                                           @RequestParam(required = false) String specialRequests,
                                           @RequestParam(required = false) List<Long> menuIds,
                                           @RequestParam(required = false) List<Integer> menuQuantities,
                                           @RequestParam(required = false) List<Long> serviceIds,
                                           @RequestParam(required = false) List<Integer> serviceQuantities,
                                           HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            if (isLoggedIn == null || !isLoggedIn) {
                response.put("success", false);
                response.put("message", "Please login to book an event");
                return response;
            }
            
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            // Resolve event by name
            if (eventName == null || eventName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Event name is required");
                return response;
            }
            com.event.event_management.entity.Event resolvedEvent =
                eventService.getEventByName(eventName.trim())
                    .orElse(null);
            if (resolvedEvent == null) {
                response.put("success", false);
                response.put("message", "No event found with name: " + eventName);
                return response;
            }
            Long eventId = resolvedEvent.getId();

            // Input validation
            if (guestCount < 1 || guestCount > 10000) {
                response.put("success", false);
                response.put("message", "Guest count must be between 1 and 10000");
                return response;
            }
            
            if (venue == null || venue.trim().isEmpty() || venue.length() > 500) {
                response.put("success", false);
                response.put("message", "Venue is required and must be less than 500 characters");
                return response;
            }

            if (venueType != null && venueType.length() > 20) {
                response.put("success", false);
                response.put("message", "Invalid venue type");
                return response;
            }

            if (bookingType == null || bookingType.trim().isEmpty() || bookingType.length() > 50) {
                response.put("success", false);
                response.put("message", "Booking type is required");
                return response;
            }
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime eventDateTime;
            try {
                eventDateTime = LocalDateTime.parse(eventDate, formatter);
                if (eventDateTime.isBefore(LocalDateTime.now())) {
                    response.put("success", false);
                    response.put("message", "Event date must be in the future");
                    return response;
                }
            } catch (Exception e) {
                response.put("success", false);
                response.put("message", "Invalid date format");
                return response;
            }
            
            Booking booking = bookingService.createBooking(
                userId, eventId, guestCount, eventDateTime, venue, venueType, bookingType,
                specialRequests, menuIds, menuQuantities, serviceIds, serviceQuantities
            );
            
            response.put("success", true);
            response.put("message", "Booking created successfully");
            response.put("bookingId", booking.getId());
            response.put("redirectUrl", "/customer/bookings");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create booking: " + e.getMessage());
        }
        
        return response;
    }
    
    @GetMapping("/customer/bookings")
    public String myBookings(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            List<Booking> bookings = bookingService.getUserBookings(userId);
            model.addAttribute("bookings", bookings);
        }
        
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "customer/my-bookings";
    }
    
    @GetMapping("/customer/bookings/{id}")
    public String viewBooking(@PathVariable Long id, Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        var bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isPresent()) {
            Booking booking = bookingOpt.get();
            Long userId = (Long) session.getAttribute("userId");
            if (booking.getUser().getId().equals(userId)) {
                model.addAttribute("booking", booking);
                model.addAttribute("userEmail", session.getAttribute("userEmail"));
                return "customer/booking-details";
            }
        }
        
        return "redirect:/customer/bookings";
    }
    
    @PostMapping("/customer/bookings/{id}/cancel")
    @ResponseBody
    public Map<String, Object> cancelBooking(@PathVariable Long id, HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            if (isLoggedIn == null || !isLoggedIn) {
                response.put("success", false);
                response.put("message", "Please login");
                return response;
            }
            
            var bookingOpt = bookingService.getBookingById(id);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                Long userId = (Long) session.getAttribute("userId");
                if (booking.getUser().getId().equals(userId)) {
                    bookingService.cancelBooking(id);
                    response.put("success", true);
                    response.put("message", "Booking cancelled successfully");
                } else {
                    response.put("success", false);
                    response.put("message", "Unauthorized");
                }
            } else {
                response.put("success", false);
                response.put("message", "Booking not found");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to cancel booking: " + e.getMessage());
        }
        
        return response;
    }
}
