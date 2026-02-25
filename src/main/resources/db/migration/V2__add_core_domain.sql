-- V2: Add Core Domain Tables (Category, Location, Product)

CREATE TABLE category (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE location (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    address     VARCHAR(255),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE product (
    id          BIGSERIAL PRIMARY KEY,
    sku         VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price       DECIMAL(19, 2),
    cost        DECIMAL(19, 2),
    category_id BIGINT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE INDEX idx_product_sku ON product(sku);
CREATE INDEX idx_product_category ON product(category_id);
