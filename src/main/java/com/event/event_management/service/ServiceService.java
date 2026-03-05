package com.event.event_management.service;

import com.event.event_management.entity.CateringService;
import com.event.event_management.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceService {
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    public List<CateringService> getAllServices() {
        return serviceRepository.findAll();
    }
    
    public List<CateringService> getAvailableServices() {
        return serviceRepository.findByIsAvailableTrue();
    }
    
    public Optional<CateringService> getServiceById(Long id) {
        return serviceRepository.findById(id);
    }
    
    public CateringService saveService(CateringService service) {
        return serviceRepository.save(service);
    }
    
    public void deleteService(Long id) {
        serviceRepository.deleteById(id);
    }
}
