package com.event.event_management.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {
    
    @GetMapping("/")
    public String landing() {
        return "landing";
    }
    
    @GetMapping("/home")
    public String home() {
        return "redirect:/dashboard";
    }
    
    @GetMapping("/about")
    public String about() {
        return "About";
    }
}
