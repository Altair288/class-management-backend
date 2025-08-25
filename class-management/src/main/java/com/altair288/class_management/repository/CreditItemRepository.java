package com.altair288.class_management.repository;

import com.altair288.class_management.model.CreditItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CreditItemRepository extends JpaRepository<CreditItem, Integer> {
    Optional<CreditItem> findByCategoryAndItemName(String category, String itemName);
    List<CreditItem> findAllByCategory(String category);
    List<CreditItem> findAllByEnabledTrue();
    boolean existsByCategory(String category);

    @Query("select c from CreditItem c where (:category is null or c.category = :category) order by c.id desc")
    List<CreditItem> findByCategoryOptional(@Param("category") String category);
}
