package com.event.event_management.controller;

import com.event.event_management.entity.Event;
import com.event.event_management.entity.EventRegistration;
import com.event.event_management.service.EventRegistrationService;
import com.event.event_management.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
public class EventRegistrationController {

    @Autowired
    private EventRegistrationService registrationService;

    @Autowired
    private EventService eventService;

    // ─── CUSTOMER: Show registration form ───────────────────────────────────
    @GetMapping("/customer/event-register/{eventId}")
    public String showRegisterForm(@PathVariable Long eventId, Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) return "redirect:/login";

        Optional<Event> event = eventService.getEventById(eventId);
        if (event.isEmpty()) return "redirect:/customer/events";

        String userEmail = (String) session.getAttribute("userEmail");
        boolean alreadyRegistered = registrationService.findByEventIdAndUserEmail(eventId, userEmail).isPresent();

        model.addAttribute("event", event.get());
        model.addAttribute("registration", new EventRegistration());
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("alreadyRegistered", alreadyRegistered);
        return "customer/event-register";
    }

    // ─── CUSTOMER: Submit registration form ────────────────────────────────
    @PostMapping("/customer/event-register/{eventId}")
    public String submitRegister(@PathVariable Long eventId,
                                 @RequestParam String name,
                                 @RequestParam String contactNumber,
                                 HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) return "redirect:/login";

        Optional<Event> event = eventService.getEventById(eventId);
        if (event.isEmpty()) return "redirect:/customer/events";

        String userEmail = (String) session.getAttribute("userEmail");
        if (registrationService.findByEventIdAndUserEmail(eventId, userEmail).isPresent()) {
            return "redirect:/customer/event-registrations"; // Already registered
        }

        EventRegistration reg = new EventRegistration();
        reg.setEvent(event.get());
        reg.setName(name);
        reg.setContactNumber(contactNumber);
        reg.setStatus("PENDING");
        reg.setUserEmail(userEmail);
        registrationService.save(reg);

        return "redirect:/customer/event-registrations";
    }

    // ─── CUSTOMER: View my registrations ───────────────────────────────────
    @GetMapping("/customer/event-registrations")
    public String myRegistrations(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) return "redirect:/login";

        model.addAttribute("registrations", registrationService.getAll());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "customer/event-registrations";
    }

    // ─── CUSTOMER: Cancel registration ─────────────────────────────────────
    @PostMapping("/customer/event-registrations/cancel/{id}")
    public String cancelRegistration(@PathVariable Long id, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) return "redirect:/login";

        Optional<EventRegistration> reg = registrationService.getById(id);
        if (reg.isPresent()) {
            EventRegistration r = reg.get();
            r.setStatus("CANCELLED");
            registrationService.save(r);
        }

        return "redirect:/customer/event-registrations";
    }

    // ─── ADMIN: View all registrations ─────────────────────────────────────
    @GetMapping("/admin/event-registrations")
    public String adminRegistrations(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) return "redirect:/login";
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) return "redirect:/customer/home";

        model.addAttribute("registrations", registrationService.getAll());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/event-registrations";
    }

    // ─── ADMIN: Approve a registration ─────────────────────────────────────
    @PostMapping("/admin/event-registrations/approve/{id}")
    public String approveRegistration(@PathVariable Long id, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) return "redirect:/login";
        String role = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(role)) return "redirect:/customer/home";

        registrationService.approve(id);
        return "redirect:/admin/event-registrations";
    }
}
