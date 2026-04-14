-- StockDual v0.5.0 - Tabla para el Formador de Precios (Planilla de Costos)
CREATE TABLE supplier_price_rule (
    id                      BIGSERIAL PRIMARY KEY,
    supplier_id             BIGINT REFERENCES supplier(id),
    base_cost               DECIMAL(19,4) DEFAULT 0,
    
    -- Descuentos (Porcentajes acumulativos)
    discount_1              DECIMAL(19,4) DEFAULT 0,
    discount_2              DECIMAL(19,4) DEFAULT 0,
    discount_3              DECIMAL(19,4) DEFAULT 0,
    
    -- Gastos en Porcentaje (Acumulativos)
    perc_expense_1          DECIMAL(19,4) DEFAULT 0,
    perc_expense_2          DECIMAL(19,4) DEFAULT 0,
    perc_expense_3          DECIMAL(19,4) DEFAULT 0,
    
    -- IVA
    iva_percentage          DECIMAL(19,4) DEFAULT 21.0,
    
    -- Gastos Fijos (Montos)
    fixed_expense_1         DECIMAL(19,4) DEFAULT 0,
    fixed_expense_2         DECIMAL(19,4) DEFAULT 0,
    
    -- Márgenes Finales
    markup_percentage       DECIMAL(19,4) DEFAULT 0,
    card_percentage         DECIMAL(19,4) DEFAULT 0,
    
    -- Resultados calculados (para facilidad de consulta)
    calculated_final_price  DECIMAL(19,4) DEFAULT 0,
    calculated_list_price   DECIMAL(19,4) DEFAULT 0,
    
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_price_rule_supplier ON supplier_price_rule(supplier_id);
