-- StockDual v0.4.6 - Índices de rendimiento y constraints de integridad

-- Índices para columnas de búsqueda frecuente
CREATE INDEX IF NOT EXISTS idx_product_wc_id ON product(wc_id);
CREATE INDEX IF NOT EXISTS idx_product_sku ON product(sku);
CREATE INDEX IF NOT EXISTS idx_product_gtin ON product(gtin);
CREATE INDEX IF NOT EXISTS idx_product_brand_id ON product(brand_id);
CREATE INDEX IF NOT EXISTS idx_product_supplier_id ON product(supplier_id);

-- Constraints de integridad para campos de stock (no permiten valores negativos)
ALTER TABLE product ADD CONSTRAINT chk_stock_local CHECK (stock_local >= 0);
ALTER TABLE product ADD CONSTRAINT chk_stock_deposito CHECK (stock_deposito >= 0);
ALTER TABLE product ADD CONSTRAINT chk_stock_galpon CHECK (stock_galpon >= 0);
ALTER TABLE product ADD CONSTRAINT chk_stock_total CHECK (stock_total >= 0);
ALTER TABLE product ADD CONSTRAINT chk_stock_wc CHECK (stock_wc >= 0);

-- Constraint para precio y costo (no negativos)
ALTER TABLE product ADD CONSTRAINT chk_price CHECK (price >= 0);
ALTER TABLE product ADD CONSTRAINT chk_cost CHECK (cost >= 0);
