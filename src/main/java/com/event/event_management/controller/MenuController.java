package com.event.event_management.controller;

import com.event.event_management.entity.Menu;
import com.event.event_management.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    @GetMapping("/customer/menu")
    public String viewMenu(@RequestParam(required = false) String category, 
                          Model model, HttpSession session) {
        Boolean isLoggedIn = (Boolean) session.getAttribute("isLoggedIn");
        if (isLoggedIn == null || !isLoggedIn) {
            return "redirect:/login";
        }
        
        List<Menu> menus;
        if (category != null && !category.isEmpty()) {
            menus = menuService.getMenusByCategory(category);
        } else {
            menus = menuService.getAvailableMenus();
        }
        
        model.addAttribute("menus", menus);
        model.addAttribute("categories", menuService.getAllCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("userEmail", session.getAttribute("userEmail"));
        
        return "customer/menu";
    }
}
