package com.event.event_management.controller;

import com.event.event_management.entity.Event;
import com.event.event_management.service.EventService;
import com.event.event_management.service.PdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Controller
public class EventController {
    @Autowired
    private EventService eventService;

    @Autowired
    private PdfService pdfService;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(LocalDateTime.class, new org.springframework.beans.propertyeditors.CustomDateEditor(
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm"), true));
    }

    // Dashboard: List of all events (admin only)
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        // Check if user is admin
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        var events = eventService.getAllEvents();
        System.out.println("=== Events Retrieved ===");
        System.out.println("Total events: " + events.size());
        for (Event event : events) {
            System.out.println("Event ID: " + event.getId() +
                    ", Name: " + event.getName() +
                    ", Venue: " + event.getVenue() +
                    ", Guests: " + event.getGuestCount() +
                    ", Status: " + event.getStatus());
        }
        System.out.println("========================");
        model.addAttribute("events", events);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "index";
    }

    // View event details
    @GetMapping("/events/{id}")
    public String viewEvent(@PathVariable Long id, Model model, HttpSession session) {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        Optional<Event> event = eventService.getEventById(id);
        if (event.isPresent()) {
            model.addAttribute("event", event.get());
            return "event-details";
        }
        return "redirect:/dashboard";
    }

    // Form for create/update
    @GetMapping("/events/form")
    public String eventForm(@RequestParam(required = false) Long id, Model model, HttpSession session) {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        if (id != null) {
            Optional<Event> event = eventService.getEventById(id);
            model.addAttribute("event", event.orElse(new Event()));
        } else {
            model.addAttribute("event", new Event());
        }
        return "event-form";
    }

    // Save (create/update)
    @PostMapping("/events/save")
    public String saveEvent(@ModelAttribute Event event,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            HttpServletRequest request) {
        if (event.getId() == null) {
            // New event - change all existing NEW events to Planned first
            eventService.changeAllNewToPlanned();
            // Set this new event as NEW
            event.setStatus("NEW");
            event.setUpdated(false);
        } else {
            // For existing events, keep their current status or set to "Planned" if null
            if (event.getStatus() == null || event.getStatus().trim().isEmpty()) {
                event.setStatus("Planned");
            }
            event.setUpdated(true);
            // Preserve existing image if no new one uploaded
            if (imageFile == null || imageFile.isEmpty()) {
                Optional<Event> existing = eventService.getEventById(event.getId());
                existing.ifPresent(e -> event.setImageUrl(e.getImageUrl()));
            }
        }

        // Handle optional image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Always save into src/main/resources/static so Spring Boot serves it at /images/events/<file>
                Path uploadPath = Paths.get(System.getProperty("user.dir"),
                        "src", "main", "resources", "static", "images", "events");
                Files.createDirectories(uploadPath);

                String originalName = imageFile.getOriginalFilename();
                String ext = (originalName != null && originalName.contains("."))
                        ? originalName.substring(originalName.lastIndexOf("."))
                        : ".jpg";
                String fileName = "event_" + UUID.randomUUID() + ext;
                Path filePath = uploadPath.resolve(fileName);
                imageFile.transferTo(filePath.toFile());
                event.setImageUrl("/images/events/" + fileName);
            } catch (IOException e) {
                System.err.println("Error saving event image: " + e.getMessage());
            }
        }

        eventService.saveEvent(event);
        return "redirect:/dashboard";
    }

    // Delete
    @GetMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return "redirect:/dashboard";
    }

    // Mark completed
    @GetMapping("/events/complete/{id}")
    public String markCompleted(@PathVariable Long id) {
        eventService.markCompleted(id);
        return "redirect:/events/" + id;
    }

    // Fix existing events without status
    @GetMapping("/events/fix-status")
    public String fixEventStatus() {
        eventService.fixEventStatus();
        return "redirect:/dashboard";
    }

    // Change NEW status to Planned
    @GetMapping("/events/mark-planned/{id}")
    public String markNewAsPlanned(@PathVariable Long id) {
        eventService.markNewAsPlanned(id);
        return "redirect:/dashboard";
    }

    // Generate PDF for event
    @GetMapping("/events/{id}/pdf")
    public ResponseEntity<byte[]> generateEventPdf(@PathVariable Long id, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return ResponseEntity.status(401).build();
        }
        // Only admin can generate PDFs
        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Event> eventOpt = eventService.getEventById(id);
        if (eventOpt.isPresent()) {
            try {
                Event event = eventOpt.get();
                // Ensure resources and updates are loaded
                if (event.getResources() != null) {
                    event.getResources().size(); // Force lazy loading
                }
                if (event.getUpdates() != null) {
                    event.getUpdates().size(); // Force lazy loading
                }
                byte[] pdfBytes = pdfService.generateEventPdf(event);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "event-" + id + "-" +
                        (event.getName() != null ? event.getName().replaceAll("[^a-zA-Z0-9]", "_") : "details")
                        + ".pdf");
                return ResponseEntity.ok().headers(headers).body(pdfBytes);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        }
        return ResponseEntity.notFound().build();
    }
}