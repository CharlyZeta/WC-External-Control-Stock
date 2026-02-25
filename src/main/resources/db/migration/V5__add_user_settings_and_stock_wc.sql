-- Agregando tabla de User Settings para preferencias de la grilla
CREATE TABLE user_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL UNIQUE,
    show_gtin BOOLEAN DEFAULT false,
    show_category BOOLEAN DEFAULT false,
    show_price BOOLEAN DEFAULT false,
    show_cost BOOLEAN DEFAULT false,
    show_tags BOOLEAN DEFAULT false,
    show_brand BOOLEAN DEFAULT false,
    show_supplier BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Agregando el campo stock_wc a la tabla de productos para comparación de inventario
ALTER TABLE product ADD COLUMN stock_wc INTEGER DEFAULT 0;
