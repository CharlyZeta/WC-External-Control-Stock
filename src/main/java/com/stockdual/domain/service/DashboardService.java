package com.stockdual.domain.service;

import com.stockdual.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;

    public long getTotalProducts() {
        return productRepository.count();
    }

    public long getTotalStock() {
        Long total = productRepository.sumTotalStock();
        return total != null ? total : 0L;
    }

    public long getLowStockCount() {
        Long alerts = productRepository.countStockAlerts();
        return alerts != null ? alerts : 0L;
    }

    public java.math.BigDecimal getTotalInventoryValue() {
        java.math.BigDecimal value = productRepository.sumInventoryValue();
        return value != null ? value : java.math.BigDecimal.ZERO;
    }
}
