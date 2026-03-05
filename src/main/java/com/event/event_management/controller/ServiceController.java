package com.event.event_management.controller;

import com.event.event_management.entity.CateringService;
import com.event.event_management.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class ServiceController {
    
    @Autowired
    private ServiceService serviceService;
    
    @GetMapping("/customer/services")
    public String viewServices(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        List<CateringService> services = serviceService.getAvailableServices();
        model.addAttribute("services", services);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        
        return "customer/services";
    }
}
