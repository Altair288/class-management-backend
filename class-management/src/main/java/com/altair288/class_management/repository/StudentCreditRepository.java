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

        // 批量汇总：按学生ID列表计算各类别分和总分
        interface StudentTotalsProjection {
                Integer getStudentId();
                Double getDe();
                Double getZhi();
                Double getTi();
                Double getMei();
                Double getLao();
                Double getTotal();
        }

        @Query("select sc.student.id as studentId, " +
                        "sum(case when sc.creditItem.category='德' then sc.score else 0 end) as de, " +
                        "sum(case when sc.creditItem.category='智' then sc.score else 0 end) as zhi, " +
                        "sum(case when sc.creditItem.category='体' then sc.score else 0 end) as ti, " +
                        "sum(case when sc.creditItem.category='美' then sc.score else 0 end) as mei, " +
                        "sum(case when sc.creditItem.category='劳' then sc.score else 0 end) as lao, " +
                        "sum(sc.score) as total " +
                        "from StudentCredit sc where sc.student.id in :ids group by sc.student.id")
        List<StudentTotalsProjection> sumByStudentIds(@Param("ids") List<Integer> studentIds);

        // 总体各类别分和
        interface CategorySumsProjection {
                Double getSumDe();
                Double getSumZhi();
                Double getSumTi();
                Double getSumMei();
                Double getSumLao();
        }

        @Query("select " +
                        "sum(case when sc.creditItem.category='德' then sc.score else 0 end) as sumDe, " +
                        "sum(case when sc.creditItem.category='智' then sc.score else 0 end) as sumZhi, " +
                        "sum(case when sc.creditItem.category='体' then sc.score else 0 end) as sumTi, " +
                        "sum(case when sc.creditItem.category='美' then sc.score else 0 end) as sumMei, " +
                        "sum(case when sc.creditItem.category='劳' then sc.score else 0 end) as sumLao " +
                        "from StudentCredit sc")
        CategorySumsProjection sumAllCategories();

        // 各等级人数（基于总分分桶）
        interface DashboardBucketsProjection {
                Long getExc();
                Long getGood();
                Long getWarning();
                Long getDanger();
        }

        @Query(value = """
                        SELECT
                            SUM(CASE WHEN t.total >= 400 THEN 1 ELSE 0 END) AS exc,
                            SUM(CASE WHEN t.total >= 350 AND t.total < 400 THEN 1 ELSE 0 END) AS good,
                            SUM(CASE WHEN t.total >= 300 AND t.total < 350 THEN 1 ELSE 0 END) AS warning,
                            SUM(CASE WHEN t.total < 300 THEN 1 ELSE 0 END) AS danger
                        FROM (
                            SELECT sc.student_id AS sid, SUM(sc.score) AS total
                            FROM student_credit sc
                            GROUP BY sc.student_id
                        ) t
                        """, nativeQuery = true)
        DashboardBucketsProjection countBuckets();
}
