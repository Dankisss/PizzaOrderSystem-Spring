CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(256) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    image_data BYTEA
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    status VARCHAR(20) DEFAULT 'PENDING',
    user_id INT NOT NULL REFERENCES USERS(id),
    address VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES USERS(id)
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    category VARCHAR(20) NOT NULL,
    capacity VARCHAR(20) NOT NULL,
    pizza_type VARCHAR(30),
    drink_type VARCHAR(30),
    is_alcoholic BOOLEAN,
    price NUMERIC(10, 2) NOT NULL,
    is_active BOOLEAN,
    total_amount NUMERIC(10, 2) DEFAULT 0,
    image_data BYTEA,
    CONSTRAINT chk_status CHECK(status in ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_category CHECK(category IN ('PIZZA', 'DRINK', 'SAUCE')),
    CONSTRAINT chk_capacity CHECK(capacity IN ('SMALL', 'MEDIUM', 'LARGE', '330ML', '500ML'))
);

CREATE TABLE orders_products (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    price_at_order_time NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES ORDERS(id),
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES PRODUCTS(id)
);