package com.altair288.class_management.service;

import com.altair288.class_management.model.Class;
import com.altair288.class_management.model.Department;
import com.altair288.class_management.repository.ClassRepository;
import com.altair288.class_management.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentService {
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private ClassRepository classRepository;

    public List<Department> list() { return departmentRepository.findAll(); }

    public Department create(Department d) { return departmentRepository.save(d); }

    public Department update(Integer id, Department d) { d.setId(id); return departmentRepository.save(d); }

    public void delete(Integer id) { departmentRepository.deleteById(id); }

    @Transactional
    public Class bindClass(Integer classId, Integer departmentId) {
        var clazz = classRepository.findById(classId).orElseThrow();
        var dept = departmentRepository.findById(departmentId).orElseThrow();
        clazz.setDepartment(dept);
        return classRepository.save(clazz);
    }
}
