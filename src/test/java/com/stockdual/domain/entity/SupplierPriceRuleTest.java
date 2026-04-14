package com.stockdual.domain.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SupplierPriceRuleTest {

    @Test
    void testCascadeCalculation() {
        // Escenario:
        // Costo: 1000
        // Descuento 1: 10% -> 900
        // Descuento 2: 5%  -> 855
        // Gasto 1 %: 10%   -> 940.5
        // IVA: 21%         -> 1138.005
        // Gasto Fijo 1: 100 -> 1238.005
        // Markup: 50%      -> 1857.0075 -> 1857.01 (Redondeado)
        // Tarjeta: 10%     -> 2042.711  -> 2042.71 (Redondeado)

        SupplierPriceRule rule = SupplierPriceRule.builder()
                .baseCost(BigDecimal.valueOf(1000))
                .discount1(BigDecimal.valueOf(10))
                .discount2(BigDecimal.valueOf(5))
                .discount3(BigDecimal.ZERO)
                .percExpense1(BigDecimal.valueOf(10))
                .percExpense2(BigDecimal.ZERO)
                .percExpense3(BigDecimal.ZERO)
                .ivaPercentage(BigDecimal.valueOf(21))
                .fixedExpense1(BigDecimal.valueOf(100))
                .fixedExpense2(BigDecimal.ZERO)
                .markupPercentage(BigDecimal.valueOf(50))
                .cardPercentage(BigDecimal.valueOf(10))
                .build();

        rule.calculate();

        // Verificamos Precio Final
        // 1000 * 0.9 * 0.95 = 855
        // 855 * 1.1 = 940.5
        // 940.5 * 1.21 = 1138.005
        // 1138.005 + 100 = 1238.005
        // 1238.005 * 1.5 = 1857.0075 -> Redondeado a 1857.01
        assertEquals(0, BigDecimal.valueOf(1857.01).compareTo(rule.getCalculatedFinalPrice()), 
            "El Precio Final no coincide con el cálculo en cascada esperado");

        // Verificamos Precio Lista
        // 1857.01 * 1.1 = 2042.711 -> Redondeado a 2042.71
        assertEquals(0, BigDecimal.valueOf(2042.71).compareTo(rule.getCalculatedListPrice()), 
            "El Precio de Lista no coincide con el cálculo en cascada esperado");
    }

    @Test
    void testNullSafety() {
        SupplierPriceRule rule = new SupplierPriceRule();
        rule.calculate();
        assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(rule.getCalculatedFinalPrice()));
        assertEquals(0, BigDecimal.ZERO.setScale(2).compareTo(rule.getCalculatedListPrice()));
    }
}
