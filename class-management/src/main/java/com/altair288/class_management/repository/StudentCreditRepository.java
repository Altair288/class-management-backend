package com.altair288.class_management.repository;

import com.altair288.class_management.model.StudentCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentCreditRepository extends JpaRepository<StudentCredit, Integer> {
    List<StudentCredit> findAllByStudent_Id(Integer studentId);
    List<StudentCredit> findAllByCreditItem_Id(Integer creditItemId);

    @Query("select coalesce(sum(sc.score), 0) from StudentCredit sc where sc.student.id = :studentId and sc.creditItem.category = :category")
    Double sumScoreByStudentAndCategory(@Param("studentId") Integer studentId, @Param("category") String category);

    @Query("select sc from StudentCredit sc where sc.student.id = :studentId and sc.creditItem.id = :itemId")
    StudentCredit findByStudentAndItem(@Param("studentId") Integer studentId, @Param("itemId") Integer itemId);

    @Query("select sc from StudentCredit sc where sc.student.id = :studentId and (:category is null or sc.creditItem.category = :category) order by sc.creditItem.id asc")
    List<StudentCredit> findByStudentAndOptionalCategory(@Param("studentId") Integer studentId, @Param("category") String category);
}
