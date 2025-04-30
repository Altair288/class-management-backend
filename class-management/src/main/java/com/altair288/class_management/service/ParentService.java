package com.altair288.class_management.service;

import com.altair288.class_management.model.Parent;
import com.altair288.class_management.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParentService {
    @Autowired
    private ParentRepository parentRepository;

    public Parent getParentById(Integer id) {
        return parentRepository.findById(id).orElse(null);
    }
    public Parent save(Parent parent) {
    // Implement the logic to save the teacher entity
    // For example, if using JPA:
    return parentRepository.save(parent);
}
}