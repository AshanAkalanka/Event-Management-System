package com.event.event_management.controller;

import com.event.event_management.entity.User;
import com.event.event_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class ProfileController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/customer/profile")
    public String viewProfile(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                model.addAttribute("user", userOpt.get());
            }
        }
        
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "customer/profile";
    }
    
    @PostMapping("/customer/profile/update")
    @ResponseBody
    public Map<String, Object> updateProfile(@RequestParam String firstName,
                                            @RequestParam String lastName,
                                            @RequestParam String email,
                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            User updatedUser = userService.updateProfile(userId, firstName, lastName, email);
            session.setAttribute("userEmail", updatedUser.getEmail());
            session.setAttribute("userName", updatedUser.getFullName());
            
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update profile: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/customer/profile/change-password")
    @ResponseBody
    public Map<String, Object> changePassword(@RequestParam String oldPassword,
                                             @RequestParam String newPassword,
                                             HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }
            
            if (newPassword.length() < 6) {
                response.put("success", false);
                response.put("message", "Password must be at least 6 characters");
                return response;
            }
            
            boolean changed = userService.changePassword(userId, oldPassword, newPassword);
            if (changed) {
                response.put("success", true);
                response.put("message", "Password changed successfully");
            } else {
                response.put("success", false);
                response.put("message", "Current password is incorrect");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to change password: " + e.getMessage());
        }
        
        return response;
    }
}
