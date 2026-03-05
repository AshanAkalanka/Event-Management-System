package com.event.event_management.service;

import com.event.event_management.entity.Update;
import com.event.event_management.repository.UpdateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UpdateService {
    @Autowired
    private UpdateRepository updateRepository;     // Repository to interact with Update table

    // Get all updates from the database
    public List<Update> getAllUpdates() {
        return updateRepository.findAll();
    }

    // Find a specific update by its ID
    public Optional<Update> getUpdateById(Long id) {
        return updateRepository.findById(id);
    }

    //Save, Update or Delete updates
    public Update saveUpdate(Update update) {
        return updateRepository.save(update);
    }

    public void deleteUpdate(Long id) {
        updateRepository.deleteById(id);
    }
}