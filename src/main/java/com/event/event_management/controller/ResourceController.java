package com.event.event_management.controller;

import com.event.event_management.entity.Resource;
import com.event.event_management.service.EventService;
import com.event.event_management.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/resources")
public class ResourceController {
    @Autowired
    private ResourceService resourceService;  // Service to handle Resource operations
    @Autowired
    private EventService eventService;        // Service to handle Event operations

    // Display list of all resources
    @GetMapping
    public String listResources(Model model, HttpSession session) {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        model.addAttribute("resources", resourceService.getAllResources());
        model.addAttribute("resource", new Resource()); // Add empty resource for form
        model.addAttribute("events", eventService.getAllEvents()); // Add events for dropdown
        return "resource-list";
    }

    // Show form for creating or editing a resource
    @GetMapping("/form")
    public String resourceForm(@RequestParam(required = false) Long id, @RequestParam(required = false) Long eventId, Model model, HttpSession session) {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        // Get resource by id, otherwise create a new resource
        Resource resource = id != null ? resourceService.getResourceById(id).orElse(new Resource()) : new Resource();

        // set the event to the resource
        if (eventId != null) {
            eventService.getEventById(eventId).ifPresent(resource::setEvent);
        }
        model.addAttribute("resource", resource);
        model.addAttribute("events", eventService.getAllEvents());  // Dropdown
        return "resource-form";
    }

    // Save resource
    @PostMapping("/save")
    public String saveResource(@ModelAttribute Resource resource, @RequestParam(required = false) Long eventId, Model model, HttpSession session) {
        // Check if user is logged in
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        // Server-side validation
        boolean hasErrors = false;
        
        // Validate name
        if (resource.getName() == null || resource.getName().trim().isEmpty()) {
            model.addAttribute("nameError", "Resource name is required");
            hasErrors = true;
        } else if (resource.getName().trim().length() < 2) {
            model.addAttribute("nameError", "Resource name must be at least 2 characters");
            hasErrors = true;
        }
        
        // Validate type
        if (resource.getType() == null || resource.getType().trim().isEmpty()) {
            model.addAttribute("typeError", "Resource type is required");
            hasErrors = true;
        }
        
        // Validate quantity
        if (resource.getQuantity() <= 0) {
            model.addAttribute("quantityError", "Quantity must be at least 1");
            hasErrors = true;
        }
        
        // Validate event
        if (eventId == null || eventId <= 0) {
            model.addAttribute("eventError", "Assigned event is required");
            hasErrors = true;
        } else {
            // Check if the event exists
            if (!eventService.getEventById(eventId).isPresent()) {
                model.addAttribute("eventError", "Selected event does not exist");
                hasErrors = true;
            }
        }
        
        // If there are validation errors, return to the form with errors
        if (hasErrors) {
            // Check if this is an update (resource has ID) or create (no ID)
            if (resource.getId() != null) {
                // This is an update, return to resource form
                model.addAttribute("events", eventService.getAllEvents());
                return "resource-form";
            } else {
                // This is a create, return to resource list
                model.addAttribute("resources", resourceService.getAllResources());
                model.addAttribute("events", eventService.getAllEvents());
                return "resource-list";
            }
        }
        
        // Set the event if eventId is provided
        if (eventId != null) {
            eventService.getEventById(eventId).ifPresent(resource::setEvent);
        }
        
        resourceService.saveResource(resource);
        return "redirect:/resources";
    }

    // Delete resource by ID
    @GetMapping("/delete/{id}")
    public String deleteResource(@PathVariable Long id) {
        resourceService.deleteResource(id);
        return "redirect:/resources";
    }
}