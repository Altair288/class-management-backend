package com.altair288.class_management.service;

import com.altair288.class_management.model.Class;
import com.altair288.class_management.repository.ClassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClassService {
    @Autowired
    private ClassRepository classRepository;

    public Class save(Class clazz) {
        return classRepository.save(clazz);
    }

    public Optional<Class> findById(Integer id) {
        return classRepository.findById(id);
    }

    public Class getById(Integer id) {
        return classRepository.findById(id).orElse(null);
    }

    public Class getByName(String className) {
        return classRepository.findByName(className).orElse(null);
    }

    public List<Class> findAll() {
        return classRepository.findAll();
    }

    public long count() {
        return classRepository.count();
    }
}
