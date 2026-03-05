package com.event.event_management.controller;

import com.event.event_management.entity.User;
import com.event.event_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "admin/login";
    }
    
    @PostMapping("/admin/login")
    @ResponseBody
    public Map<String, Object> authenticateAdmin(@RequestParam String email, 
                                                @RequestParam String password,
                                                HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> user = userService.authenticateUser(email, password);
            if (user.isPresent() && "ADMIN".equals(user.get().getRole())) {
                session.setAttribute("isLoggedIn", true);
                session.setAttribute("userEmail", email);
                session.setAttribute("userId", user.get().getId());
                session.setAttribute("userName", user.get().getFullName());
                session.setAttribute("userRole", "ADMIN");
                session.setAttribute("loginTime", System.currentTimeMillis());
                
                response.put("success", true);
                response.put("message", "Admin login successful");
                response.put("redirectUrl", "/dashboard");
            } else {
                response.put("success", false);
                response.put("message", "Invalid admin credentials");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed. Please try again.");
        }
        
        return response;
    }
}
