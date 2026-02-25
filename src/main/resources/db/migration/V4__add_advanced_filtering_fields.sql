CREATE TABLE brand (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE
);

CREATE TABLE supplier (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL UNIQUE,
    contact_info VARCHAR(500)
);

ALTER TABLE product
    ADD COLUMN brand_id BIGINT,
    ADD COLUMN supplier_id BIGINT,
    ADD COLUMN tags VARCHAR(1000),
    ADD COLUMN attributes VARCHAR(2000),
    ADD CONSTRAINT fk_product_brand FOREIGN KEY (brand_id) REFERENCES brand(id),
    ADD CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES supplier(id);
