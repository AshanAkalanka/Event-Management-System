package com.event.event_management.service;

import com.event.event_management.entity.Resource;
import com.event.event_management.repository.ResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {
    @Autowired
    private ResourceRepository resourceRepository;       // Repository to interact with Resource table

    // Get all resources from the database
    public List<Resource> getAllResources() {
        return resourceRepository.findAll();
    }

    // Save, Update or Delete Resource
    public Optional<Resource> getResourceById(Long id) {
        return resourceRepository.findById(id);
    }

    public Resource saveResource(Resource resource) {
        return resourceRepository.save(resource);
    }

    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }
}