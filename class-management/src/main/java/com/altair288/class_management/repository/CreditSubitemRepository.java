package com.altair288.class_management.repository;

import com.altair288.class_management.model.CreditSubitem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditSubitemRepository extends JpaRepository<CreditSubitem, Integer> {
    List<CreditSubitem> findByItemId(Integer itemId);
    Optional<CreditSubitem> findByItemIdAndSubitemName(Integer itemId, String subitemName);
}