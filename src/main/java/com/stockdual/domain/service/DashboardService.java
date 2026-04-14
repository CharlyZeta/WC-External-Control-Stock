package com.stockdual.domain.service;

import com.stockdual.domain.entity.Product;
import com.stockdual.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProductRepository productRepository;

    public long getTotalProducts() {
        try {
            return productRepository.count();
        } catch (Exception e) {
            return 0L;
        }
    }

    public long getTotalStock() {
        try {
            Long total = productRepository.sumTotalStock();
            return total != null ? total : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public long getLowStockCount() {
        try {
            Long alerts = productRepository.countStockAlerts();
            return alerts != null ? alerts : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public java.math.BigDecimal getTotalInventoryValue() {
        try {
            java.math.BigDecimal value = productRepository.sumInventoryValue();
            return value != null ? value : java.math.BigDecimal.ZERO;
        } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }

    public long getOutOfStockCount() {
        try {
            Long count = productRepository.countOutOfStock();
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public long getCriticalStockCount(int threshold) {
        try {
            Long count = productRepository.countCriticalStock(threshold);
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    /** Stock total en Local */
    public long getStockLocal() {
        List<Product> products = productRepository.findAll();
        return products.stream().mapToLong(p -> p.getStockLocal() != null ? p.getStockLocal() : 0).sum();
    }

    /** Stock total en Depósito */
    public long getStockDeposito() {
        List<Product> products = productRepository.findAll();
        return products.stream().mapToLong(p -> p.getStockDeposito() != null ? p.getStockDeposito() : 0).sum();
    }

    /** Stock total en Galpón */
    public long getStockGalpon() {
        List<Product> products = productRepository.findAll();
        return products.stream().mapToLong(p -> p.getStockGalpon() != null ? p.getStockGalpon() : 0).sum();
    }

    /** Porcentaje de productos con imagen cargada */
    public int getImageCoveragePercent() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) return 0;
        long withImage = products.stream().filter(p -> p.getImageUrl() != null && !p.getImageUrl().isEmpty()).count();
        return (int) Math.round((double) withImage / products.size() * 100);
    }

    /** Cantidad de marcas registradas */
    public long getBrandCount() {
        return productRepository.findAll().stream()
                .filter(p -> p.getBrand() != null).map(p -> p.getBrand().getName()).distinct().count();
    }

    /** Cantidad de proveedores registrados */
    public long getSupplierCount() {
        return productRepository.findAll().stream()
                .filter(p -> p.getSupplier() != null).map(p -> p.getSupplier().getName()).distinct().count();
    }
}
