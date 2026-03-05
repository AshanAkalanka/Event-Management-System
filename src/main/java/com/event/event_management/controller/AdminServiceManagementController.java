package com.event.event_management.controller;

import com.event.event_management.entity.CateringService;
import com.event.event_management.service.ServiceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

@Controller
public class AdminServiceManagementController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/admin/services")
    public String services(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        model.addAttribute("services", serviceService.getAllServices());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/services";
    }

    @GetMapping("/admin/services/new")
    public String newService(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        model.addAttribute("service", new CateringService());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/service-form";
    }

    @GetMapping("/admin/services/{id}/edit")
    public String editService(@PathVariable Long id, Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        CateringService service = serviceService.getServiceById(id).orElse(new CateringService());
        model.addAttribute("service", service);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/service-form";
    }

    @PostMapping("/admin/services/save")
    public String saveService(@RequestParam(required = false) Long id,
                              @RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam java.math.BigDecimal price,
                              @RequestParam String unit,
                              @RequestParam(required = false) Boolean isAvailable,
                              @RequestParam(required = false) MultipartFile image,
                              HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        CateringService service = (id != null) ? serviceService.getServiceById(id).orElse(new CateringService()) : new CateringService();
        service.setName(name);
        service.setDescription(description);
        service.setPrice(price);
        service.setUnit(unit);
        service.setIsAvailable(isAvailable != null ? isAvailable : true);

        if (image != null && !image.isEmpty()) {
            try {
                Path uploadsDir = Path.of("uploads").toAbsolutePath().normalize();
                Files.createDirectories(uploadsDir);
                String original = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image";
                String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
                String filename = "service-" + LocalDateTime.now().toString().replace(":", "-") + "-" + safeName;
                Path target = uploadsDir.resolve(filename);
                image.transferTo(target);
                service.setImageUrl("/uploads/" + filename);
            } catch (Exception ignored) {
            }
        }

        serviceService.saveService(service);
        return "redirect:/admin/services";
    }

    @PostMapping("/admin/services/{id}/delete")
    public String deleteService(@PathVariable Long id, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        serviceService.deleteService(id);
        return "redirect:/admin/services";
    }
}
