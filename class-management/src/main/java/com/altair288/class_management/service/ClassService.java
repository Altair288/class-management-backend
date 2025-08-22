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

    // 新增：获取或创建“未分班”班级（名称唯一）
    public Class getOrCreateUnassignedClass() {
        return classRepository.findByName("未分班")
                .orElseGet(() -> {
                    Class c = new Class();
                    c.setName("未分班");
                    c.setGrade("N/A"); // 年级占位，按需调整
                    // teacher 可为 null
                    return classRepository.save(c);
                });
    }
}
