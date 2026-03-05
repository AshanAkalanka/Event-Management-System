package com.event.event_management.service;

import com.event.event_management.entity.Menu;
import com.event.event_management.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MenuService {
    
    @Autowired
    private MenuRepository menuRepository;
    
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }
    
    public List<Menu> getAvailableMenus() {
        return menuRepository.findByIsAvailableTrue();
    }
    
    public List<Menu> getMenusByCategory(String category) {
        return menuRepository.findByCategoryAndIsAvailableTrue(category);
    }
    
    public Optional<Menu> getMenuById(Long id) {
        return menuRepository.findById(id);
    }
    
    public Menu saveMenu(Menu menu) {
        return menuRepository.save(menu);
    }
    
    public void deleteMenu(Long id) {
        menuRepository.deleteById(id);
    }
    
    public List<String> getAllCategories() {
        return List.of("TRADITIONAL", "PARTY", "WEDDING", "BEVERAGE", "DESSERT");
    }
}
