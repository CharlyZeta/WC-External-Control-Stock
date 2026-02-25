-- StockDual v0.1.0 - Esquema inicial

-- Tabla de usuarios admin
CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100),
    role            VARCHAR(30)  NOT NULL DEFAULT 'ADMIN',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Tabla de configuraciones clave-valor
CREATE TABLE app_setting (
    id              BIGSERIAL PRIMARY KEY,
    setting_key     VARCHAR(100) NOT NULL UNIQUE,
    setting_value   TEXT,
    description     VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Índices
CREATE INDEX idx_app_user_username ON app_user(username);
CREATE INDEX idx_app_setting_key ON app_setting(setting_key);
