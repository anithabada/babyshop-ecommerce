-- =========================================================
-- BabyShop E-Commerce Database Schema
-- MySQL 8.0+
-- Run this manually with:  mysql -u root -p < schema.sql
-- (the application also runs these same statements automatically
--  on startup using CREATE TABLE IF NOT EXISTS, so this file is
--  mainly for reference / manual setup)
-- =========================================================

CREATE DATABASE IF NOT EXISTS babyshop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE babyshop_db;

-- ---------------------------------------------------------
-- users
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL,
    email         VARCHAR(150)  NOT NULL UNIQUE,
    phone         VARCHAR(20)   NOT NULL,
    password      VARCHAR(255)  NOT NULL,      -- BCrypt hash
    role          VARCHAR(20)   NOT NULL DEFAULT 'ROLE_CUSTOMER',
    enabled       TINYINT(1)    NOT NULL DEFAULT 1,
    created_at    DATETIME      NOT NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------
-- categories
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100)  NOT NULL UNIQUE,
    description   VARCHAR(255)
) ENGINE=InnoDB;

-- ---------------------------------------------------------
-- products
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(150)   NOT NULL,
    description     VARCHAR(1000),
    price           DECIMAL(10,2)  NOT NULL,
    stock_quantity  INT            NOT NULL DEFAULT 0,
    image_url       VARCHAR(500),
    brand           VARCHAR(100)   NOT NULL,
    category_id     BIGINT         NOT NULL,
    active          TINYINT(1)     NOT NULL DEFAULT 1,
    created_at      DATETIME       NOT NULL,
    updated_at      DATETIME,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------
-- cart  (one cart per user)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS cart (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------
-- cart_items
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS cart_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id       BIGINT NOT NULL,
    product_id    BIGINT NOT NULL,
    quantity      INT    NOT NULL,
    CONSTRAINT fk_cartitems_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    CONSTRAINT fk_cartitems_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------
-- orders
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id            BIGINT NOT NULL,
    total_amount       DECIMAL(10,2) NOT NULL,
    status             VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    shipping_address   VARCHAR(500)  NOT NULL,
    contact_phone      VARCHAR(20)   NOT NULL,
    created_at         DATETIME      NOT NULL,
    updated_at         DATETIME,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------
-- order_items  (snapshot of product name/price at purchase time)
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT NOT NULL,
    product_id    BIGINT,
    product_name  VARCHAR(150)  NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    quantity      INT           NOT NULL,
    subtotal      DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_orderitems_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitems_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE SET NULL
) ENGINE=InnoDB;
