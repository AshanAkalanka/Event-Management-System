package com.event.event_management.controller;

import com.event.event_management.entity.Menu;
import com.event.event_management.service.MenuService;
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
public class AdminMenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/admin/menus")
    public String menus(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        model.addAttribute("menus", menuService.getAllMenus());
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/menus";
    }

    @GetMapping("/admin/menus/new")
    public String newMenu(Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        model.addAttribute("menu", new Menu());
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/menu-form";
    }

    @GetMapping("/admin/menus/{id}/edit")
    public String editMenu(@PathVariable Long id, Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        Menu menu = menuService.getMenuById(id).orElse(new Menu());
        model.addAttribute("menu", menu);
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        return "admin/menu-form";
    }

    @PostMapping("/admin/menus/save")
    public String saveMenu(@RequestParam(required = false) Long id,
                           @RequestParam String name,
                           @RequestParam String category,
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

        Menu menu = (id != null) ? menuService.getMenuById(id).orElse(new Menu()) : new Menu();
        menu.setName(name);
        menu.setCategory(category);
        menu.setDescription(description);
        menu.setPrice(price);
        menu.setUnit(unit);
        menu.setIsAvailable(isAvailable != null ? isAvailable : true);

        if (image != null && !image.isEmpty()) {
            try {
                Path uploadsDir = Path.of("uploads").toAbsolutePath().normalize();
                Files.createDirectories(uploadsDir);
                String original = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image";
                String safeName = original.replaceAll("[^a-zA-Z0-9._-]", "_");
                String filename = "menu-" + LocalDateTime.now().toString().replace(":", "-") + "-" + safeName;
                Path target = uploadsDir.resolve(filename);
                image.transferTo(target);
                menu.setImageUrl("/uploads/" + filename);
            } catch (Exception ignored) {
            }
        }

        menuService.saveMenu(menu);
        return "redirect:/admin/menus";
    }

    @PostMapping("/admin/menus/{id}/delete")
    public String deleteMenu(@PathVariable Long id, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }

        String userRole = (String) session.getAttribute("userRole");
        if (userRole == null || !"ADMIN".equals(userRole)) {
            return "redirect:/customer/home";
        }

        menuService.deleteMenu(id);
        return "redirect:/admin/menus";
    }
}
