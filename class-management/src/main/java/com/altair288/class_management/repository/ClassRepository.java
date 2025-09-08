package com.altair288.class_management.repository;

import com.altair288.class_management.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClassRepository extends JpaRepository<Class, Integer> {
    Optional<Class> findByName(String name);
    // 按班主任教师查询其管理的班级列表
    java.util.List<Class> findByTeacher_Id(Integer teacherId);
    // 批量按教师ID查询（避免 N+1）
    java.util.List<Class> findByTeacher_IdIn(java.util.List<Integer> teacherIds);

    // 携带系部信息的批量查询（避免对 department 再次触发延迟加载）
    @org.springframework.data.jpa.repository.Query("select c from Class c left join fetch c.department where c.teacher.id in :teacherIds")
    java.util.List<Class> findWithDepartmentByTeacherIds(@org.springframework.data.repository.query.Param("teacherIds") java.util.List<Integer> teacherIds);

    @org.springframework.data.jpa.repository.Query("select c from Class c left join fetch c.department where c.id in :ids")
    java.util.List<Class> findWithDepartmentByIds(@org.springframework.data.repository.query.Param("ids") java.util.List<Integer> ids);
}
