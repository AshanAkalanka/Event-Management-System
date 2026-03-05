package com.event.event_management.controller;

import com.event.event_management.entity.Booking;
import com.event.event_management.service.BookingService;
import com.event.event_management.service.EventService;
import com.event.event_management.service.MenuService;
import com.event.event_management.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class CustomerController {
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private ServiceService serviceService;

    @Autowired
    private EventService eventService;
    
    @GetMapping("/customer/home")
    public String customerHome(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        Long userId = (Long) session.getAttribute("userId");
        List<Booking> bookings = new java.util.ArrayList<>();
        if (userId != null) {
            try {
                bookings = bookingService.getUserBookings(userId);
            } catch (Exception e) {
                System.err.println("Error fetching bookings: " + e.getMessage());
                bookings = new java.util.ArrayList<>();
            }
        }
        model.addAttribute("bookings", bookings);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        model.addAttribute("userName", session.getAttribute("userName"));
        return "customer/home";
    }

    @GetMapping("/customer/events")
    public String customerEvents(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        model.addAttribute("events", eventService.getAllEvents());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        model.addAttribute("userName", session.getAttribute("userName"));
        return "customer/events";
    }
}
