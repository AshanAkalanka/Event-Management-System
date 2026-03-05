package com.event.event_management.service;

import com.event.event_management.entity.User;
import com.event.event_management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    // Register a new user
    public User registerUser(String email, String password, String firstName, String lastName) {
        // Check if a user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email " + email + " already exists");
        }
        
        // Create a new user
        User user = new User(email, password, firstName, lastName);
        return userRepository.save(user);
    }
    
    // Authenticate user login
    public Optional<User> authenticateUser(String email, String password) {
        return userRepository.findByEmailAndPasswordAndIsActiveTrue(email, password);
    }
    
    // Get user by email and ID
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email);
    }
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Update user
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    // Delete user
    public void deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            user.get().setIsActive(false);
            userRepository.save(user.get());
        }
    }
    
    // Check if email exists
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    // Change password
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(oldPassword)) {
                user.setPassword(newPassword);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
    
    // Update user profile
    public User updateProfile(Long userId, String firstName, String lastName, String email) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            // Only update email if it doesn't exist for another user
            if (!email.equals(user.getEmail()) && !userRepository.existsByEmail(email)) {
                user.setEmail(email);
            }
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }
    
    // Initialize admin user if it doesn't exist
    public void initializeAdminUser() {
        String adminEmail = "admin@email.com";
        String adminPassword = "admin1234";
        
        // Check if admin user exists (checking all users, not just active ones)
        Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
        if (adminOpt.isEmpty()) {
            // Create new admin user
            User admin = new User(adminEmail, adminPassword, "Admin", "User");
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("✓ Admin user created successfully!");
            System.out.println("  Email: " + adminEmail);
            System.out.println("  Password: " + adminPassword);
        } else {
            // Update existing admin user to ensure correct password and role
            User admin = adminOpt.get();
            boolean updated = false;
            if (!adminPassword.equals(admin.getPassword())) {
                admin.setPassword(adminPassword);
                updated = true;
            }
            if (!"ADMIN".equals(admin.getRole())) {
                admin.setRole("ADMIN");
                updated = true;
            }
            if (admin.getIsActive() == null || !admin.getIsActive()) {
                admin.setIsActive(true);
                updated = true;
            }
            if (updated) {
                userRepository.save(admin);
                System.out.println("✓ Admin user updated successfully!");
            } else {
                System.out.println("✓ Admin user already exists and is configured correctly.");
            }
            System.out.println("  Email: " + adminEmail);
            System.out.println("  Password: " + adminPassword);
        }
    }
}
