package com.erp.repository;

import com.erp.entity.BusinessInsight;
import com.erp.enums.InsightCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessInsightRepository extends JpaRepository<BusinessInsight, Long> {
    List<BusinessInsight> findByCategoryOrderByGeneratedAtDesc(InsightCategory category);

    List<BusinessInsight> findAllByOrderByGeneratedAtDesc();
}
