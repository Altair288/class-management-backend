package com.altair288.class_management.repository;

import com.altair288.class_management.model.CreditSubitem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreditSubitemRepository extends JpaRepository<CreditSubitem, Integer> {
    List<CreditSubitem> findByItemId(Integer itemId);
    Optional<CreditSubitem> findByItemIdAndSubitemName(Integer itemId, String subitemName);

    @Query("select coalesce(sum(cs.weight),0) from CreditSubitem cs where cs.item.id = :itemId and cs.enabled = true")
    Double sumEnabledWeightByItem(@Param("itemId") Integer itemId);
}