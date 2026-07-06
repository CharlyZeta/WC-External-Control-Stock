-- V13__add_product_creator_fields.sql
-- Agregar columnas para medidas físicas, descripción corta y metadatos de Google y Facebook

ALTER TABLE product ADD COLUMN weight NUMERIC(19, 2);
ALTER TABLE product ADD COLUMN length NUMERIC(19, 2);
ALTER TABLE product ADD COLUMN width NUMERIC(19, 2);
ALTER TABLE product ADD COLUMN height NUMERIC(19, 2);
ALTER TABLE product ADD COLUMN short_description TEXT;
ALTER TABLE product ADD COLUMN google_metadata TEXT;
ALTER TABLE product ADD COLUMN facebook_metadata TEXT;

-- Agregar columnas de configuración de Gemini y Base URL a user_settings
ALTER TABLE user_settings ADD COLUMN gemini_api_key VARCHAR(255);
ALTER TABLE user_settings ADD COLUMN image_enhance_prompt TEXT;
ALTER TABLE user_settings ADD COLUMN app_base_url VARCHAR(255);
