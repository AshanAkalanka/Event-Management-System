package com.event.event_management.repository;

import com.event.event_management.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByCategory(String category);
    List<Menu> findByCategoryAndIsAvailableTrue(String category);
    List<Menu> findByIsAvailableTrue();
}
