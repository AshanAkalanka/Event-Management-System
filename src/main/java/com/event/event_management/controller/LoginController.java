package com.event.event_management.controller;

import com.event.event_management.entity.User;
import com.event.event_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "Event-login";
    }

    @PostMapping("/login")
    @ResponseBody
    public Map<String, Object> authenticate(@RequestParam String email, 
                                          @RequestParam String password,
                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> user = userService.authenticateUser(email, password);
            if (user.isPresent()) {
                // Valid credentials
                session.setAttribute("isLoggedIn", true);
                session.setAttribute("userEmail", email);
                session.setAttribute("userId", user.get().getId());
                session.setAttribute("userName", user.get().getFullName());
                session.setAttribute("userRole", user.get().getRole());
                session.setAttribute("loginTime", System.currentTimeMillis());
                
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("userRole", user.get().getRole());
                // Redirect based on role
                if ("ADMIN".equals(user.get().getRole())) {
                    response.put("redirectUrl", "/dashboard");
                } else {
                    response.put("redirectUrl", "/customer/home");
                }
            } else {
                // Invalid credentials
                response.put("success", false);
                response.put("message", "Invalid email or password");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed. Please try again.");
        }
        
        return response;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?logout=true";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> register(@RequestParam String firstName,
                                       @RequestParam String lastName,
                                       @RequestParam String email,
                                       @RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if email already exists
            if (userService.emailExists(email)) {
                response.put("success", false);
                response.put("message", "Email already exists. Please use a different email.");
                return response;
            }
            
            // Register new user
            User newUser = userService.registerUser(email, password, firstName, lastName);
            
            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("userId", newUser.getId());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/check-auth")
    @ResponseBody
    public Map<String, Object> checkAuth(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn != null && isLoggedIn) {
            response.put("authenticated", true);
            response.put("userEmail", session.getAttribute("userEmail"));
            response.put("userName", session.getAttribute("userName"));
        } else {
            response.put("authenticated", false);
        }
        
        return response;
    }
}
