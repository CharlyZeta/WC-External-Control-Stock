-- StockDual v0.4.7 - Añadir umbral de stock crítico a la configuración de usuario
ALTER TABLE user_settings ADD COLUMN IF NOT EXISTS critical_stock_threshold INTEGER DEFAULT 5;
