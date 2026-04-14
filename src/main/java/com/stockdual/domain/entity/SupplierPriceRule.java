package com.stockdual.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_price_rule")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SupplierPriceRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "base_cost")
    @Builder.Default
    private BigDecimal baseCost = BigDecimal.ZERO;

    @Column(name = "discount_1")
    @Builder.Default
    private BigDecimal discount1 = BigDecimal.ZERO;
    @Column(name = "discount_2")
    @Builder.Default
    private BigDecimal discount2 = BigDecimal.ZERO;
    @Column(name = "discount_3")
    @Builder.Default
    private BigDecimal discount3 = BigDecimal.ZERO;

    @Column(name = "perc_expense_1")
    @Builder.Default
    private BigDecimal percExpense1 = BigDecimal.ZERO;
    @Column(name = "perc_expense_2")
    @Builder.Default
    private BigDecimal percExpense2 = BigDecimal.ZERO;
    @Column(name = "perc_expense_3")
    @Builder.Default
    private BigDecimal percExpense3 = BigDecimal.ZERO;

    @Column(name = "iva_percentage")
    @Builder.Default
    private BigDecimal ivaPercentage = BigDecimal.valueOf(21);

    @Column(name = "fixed_expense_1")
    @Builder.Default
    private BigDecimal fixedExpense1 = BigDecimal.ZERO;
    @Column(name = "fixed_expense_2")
    @Builder.Default
    private BigDecimal fixedExpense2 = BigDecimal.ZERO;

    @Column(name = "markup_percentage")
    @Builder.Default
    private BigDecimal markupPercentage = BigDecimal.ZERO;
    @Column(name = "card_percentage")
    @Builder.Default
    private BigDecimal cardPercentage = BigDecimal.ZERO;

    @Column(name = "calculated_final_price")
    @Builder.Default
    private BigDecimal calculatedFinalPrice = BigDecimal.ZERO;
    @Column(name = "calculated_list_price")
    @Builder.Default
    private BigDecimal calculatedListPrice = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void calculate() {
        this.updatedAt = LocalDateTime.now();
        
        // Asegurar que no hay nulos antes de empezar
        BigDecimal current = baseCost != null ? baseCost : BigDecimal.ZERO;

        // 1. Descuentos acumulativos (cascada)
        current = applyDiscount(current, discount1);
        current = applyDiscount(current, discount2);
        current = applyDiscount(current, discount3);

        // 2. Gastos en porcentaje acumulativos
        current = applyMarkup(current, perc_safe(percExpense1));
        current = applyMarkup(current, perc_safe(percExpense2));
        current = applyMarkup(current, perc_safe(percExpense3));

        // 3. IVA
        current = applyMarkup(current, perc_safe(ivaPercentage));

        // 4. Gastos fijos (montos)
        current = current.add(fixedExpense1 != null ? fixedExpense1 : BigDecimal.ZERO);
        current = current.add(fixedExpense2 != null ? fixedExpense2 : BigDecimal.ZERO);

        // 5. Precio Final (con Markup)
        this.calculatedFinalPrice = applyMarkup(current, perc_safe(markupPercentage))
                .setScale(2, RoundingMode.HALF_UP);

        // 6. Precio de Lista (con Tarjeta sobre el Final)
        this.calculatedListPrice = applyMarkup(this.calculatedFinalPrice, perc_safe(cardPercentage))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal perc_safe(BigDecimal val) {
        return val != null ? val : BigDecimal.ZERO;
    }

    private BigDecimal applyDiscount(BigDecimal amount, BigDecimal pct) {
        if (amount == null) return BigDecimal.ZERO;
        if (pct == null || pct.compareTo(BigDecimal.ZERO) == 0) return amount;
        BigDecimal factor = BigDecimal.ONE.subtract(pct.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
        return amount.multiply(factor);
    }

    private BigDecimal applyMarkup(BigDecimal amount, BigDecimal pct) {
        if (amount == null) return BigDecimal.ZERO;
        if (pct == null || pct.compareTo(BigDecimal.ZERO) == 0) return amount;
        BigDecimal factor = BigDecimal.ONE.add(pct.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));
        return amount.multiply(factor);
    }
}
