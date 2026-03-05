package com.event.event_management.controller;

import com.event.event_management.entity.ChatMessage;
import com.event.event_management.entity.User;
import com.event.event_management.repository.ChatMessageRepository;
import com.event.event_management.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ChatApiController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/api/chat/messages")
    public Map<String, Object> getCustomerMessages(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            response.put("success", false);
            response.put("message", "Please login");
            return response;
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        List<ChatMessage> messages = chatMessageRepository.findByCustomerOrderByCreatedAtAsc(userOpt.get());
        response.put("success", true);
        response.put("messages", toDtoList(messages));
        return response;
    }

    @PostMapping("/api/chat/messages")
    public Map<String, Object> sendCustomerMessage(@RequestParam("message") String message, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            response.put("success", false);
            response.put("message", "Please login");
            return response;
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        String trimmed = message != null ? message.trim() : "";
        if (trimmed.isEmpty() || trimmed.length() > 2000) {
            response.put("success", false);
            response.put("message", "Message is required");
            return response;
        }

        Optional<User> userOpt = userService.getUserById(userId);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCustomer(userOpt.get());
        chatMessage.setSenderRole("USER");
        chatMessage.setMessage(trimmed);
        chatMessageRepository.save(chatMessage);

        response.put("success", true);
        return response;
    }

    @GetMapping("/api/admin/chat/messages")
    public Map<String, Object> getAdminMessages(@RequestParam("customerId") Long customerId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            response.put("success", false);
            response.put("message", "Please login");
            return response;
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        Optional<User> customerOpt = userService.getUserById(customerId);
        if (customerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Customer not found");
            return response;
        }

        List<ChatMessage> messages = chatMessageRepository.findByCustomerOrderByCreatedAtAsc(customerOpt.get());
        
        // Mark messages as read
        boolean updated = false;
        for (ChatMessage msg : messages) {
            if ("USER".equals(msg.getSenderRole()) && (msg.getIsRead() == null || !msg.getIsRead())) {
                msg.setIsRead(true);
                updated = true;
            }
        }
        if (updated) {
            chatMessageRepository.saveAll(messages);
        }

        response.put("success", true);
        response.put("messages", toDtoList(messages));
        return response;
    }

    @PostMapping("/api/admin/chat/messages")
    public Map<String, Object> sendAdminMessage(@RequestParam("customerId") Long customerId,
            @RequestParam("message") String message,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            response.put("success", false);
            response.put("message", "Please login");
            return response;
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        String trimmed = message != null ? message.trim() : "";
        if (trimmed.isEmpty() || trimmed.length() > 2000) {
            response.put("success", false);
            response.put("message", "Message is required");
            return response;
        }

        Optional<User> customerOpt = userService.getUserById(customerId);
        if (customerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Customer not found");
            return response;
        }

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCustomer(customerOpt.get());
        chatMessage.setSenderRole("ADMIN");
        chatMessage.setMessage(trimmed);
        chatMessageRepository.save(chatMessage);

        response.put("success", true);
        return response;
    }

    @GetMapping("/api/admin/chat/unread-status")
    public Map<String, Object> getUnreadStatus(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            response.put("success", false);
            response.put("message", "Please login");
            return response;
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return response;
        }

        List<Long> unreadCustomerIds = chatMessageRepository.findCustomersWithUnreadMessages();
        response.put("success", true);
        response.put("unreadCustomerIds", unreadCustomerIds);
        return response;
    }

    private List<Map<String, Object>> toDtoList(List<ChatMessage> messages) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (ChatMessage m : messages) {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", m.getId());
            dto.put("senderRole", m.getSenderRole());
            dto.put("message", m.getMessage());
            dto.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
            dto.put("customerId", m.getCustomer() != null ? m.getCustomer().getId() : null);
            out.add(dto);
        }
        return out;
    }
}
