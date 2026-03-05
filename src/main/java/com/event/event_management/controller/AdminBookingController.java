package com.event.event_management.controller;

import com.event.event_management.entity.Booking;
import com.event.event_management.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminBookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/admin/bookings")
    public String adminBookings(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/bookings";
    }

    @PostMapping("/admin/bookings/{id}/approve")
    @ResponseBody
    public Map<String, Object> approve(@PathVariable Long id,
            @RequestParam(value = "adminNote", required = false) String adminNote,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            if (isLoggedIn == null || !isLoggedIn) {
                response.put("success", false);
                response.put("message", "Please login");
                return response;
            }

            String userRole = (String) session.getAttribute("userRole");
            if (userRole == null || !"ADMIN".equals(userRole)) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return response;
            }

            bookingService.approveBooking(id, adminNote);
            response.put("success", true);
            response.put("message", "Booking approved");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to approve booking");
        }

        return response;
    }

    @PostMapping("/admin/bookings/{id}/decline")
    @ResponseBody
    public Map<String, Object> decline(@PathVariable Long id,
            @RequestParam(value = "adminNote", required = false) String adminNote,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
            if (isLoggedIn == null || !isLoggedIn) {
                response.put("success", false);
                response.put("message", "Please login");
                return response;
            }

            String userRole = (String) session.getAttribute("userRole");
            if (userRole == null || !"ADMIN".equals(userRole)) {
                response.put("success", false);
                response.put("message", "Unauthorized");
                return response;
            }

            bookingService.declineBooking(id, adminNote);
            response.put("success", true);
            response.put("message", "Booking declined");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to decline booking");
        }

        return response;
    }
}
