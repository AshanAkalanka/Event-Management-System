package com.event.event_management.controller;

import com.event.event_management.repository.ChatMessageRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @GetMapping("/customer/chat")
    public String customerChat(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole != null && "ADMIN".equals(userRole)) {
            return "redirect:/admin/chat";
        }

        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "customer/chat";
    }

    @GetMapping("/admin/chat")
    public String adminChat(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        model.addAttribute("customers", chatMessageRepository.findDistinctCustomers());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/chat";
    }
}
