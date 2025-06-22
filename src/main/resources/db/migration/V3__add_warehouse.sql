CREATE TABLE WAREHOUSES (
    id SERIAL PRIMARY KEY,
    name VARCHAR(30) UNIQUE NOT NULL,
    location VARCHAR(50) NOT NULL
);

CREATE TABLE INVENTORY (
    id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    warehouse_id INTEGER NOT NULL,
    amount INTEGER,
    CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(id),
    CONSTRAINT fk_inventory_warehouse FOREIGN KEY (warehouse_id) REFERENCES WAREHOUSES(id)
);