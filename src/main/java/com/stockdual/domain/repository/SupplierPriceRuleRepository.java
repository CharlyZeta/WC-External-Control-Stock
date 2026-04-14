package com.stockdual.domain.repository;

import com.stockdual.domain.entity.SupplierPriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierPriceRuleRepository extends JpaRepository<SupplierPriceRule, Long> {
}
