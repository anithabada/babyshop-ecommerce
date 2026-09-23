package com.babyshop.db;

import com.babyshop.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Very small JDBC helper: opens a fresh Connection per call.
 * (No pooling library is used since we're avoiding external frameworks -
 * for a learning/demo project this is perfectly adequate. Each DAO method
 * opens a connection in a try-with-resources block and closes it immediately.)
 */
public final class Db {

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MariaDB JDBC driver not found on classpath", e);
        }
    }

    private Db() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(AppConfig.DB_URL, AppConfig.DB_USER, AppConfig.DB_PASSWORD);
    }

    /**
     * Creates all tables (IF NOT EXISTS) on startup, so the app works
     * even if the operator hasn't manually run schema.sql first.
     */
    public static void initSchema() {
        String[] statements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                name          VARCHAR(100)  NOT NULL,
                email         VARCHAR(150)  NOT NULL UNIQUE,
                phone         VARCHAR(20)   NOT NULL,
                password      VARCHAR(255)  NOT NULL,
                role          VARCHAR(20)   NOT NULL DEFAULT 'ROLE_CUSTOMER',
                enabled       TINYINT(1)    NOT NULL DEFAULT 1,
                created_at    DATETIME      NOT NULL
            ) ENGINE=InnoDB
            """,
            """
            CREATE TABLE IF NOT EXISTS categories (
                id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                name          VARCHAR(100)  NOT NULL UNIQUE,
                description   VARCHAR(255)
            ) ENGINE=InnoDB
            """,
            """
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
            ) ENGINE=InnoDB
            """,
            """
            CREATE TABLE IF NOT EXISTS cart (
                id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id       BIGINT NOT NULL UNIQUE,
                CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            ) ENGINE=InnoDB
            """,
            """
            CREATE TABLE IF NOT EXISTS cart_items (
                id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                cart_id       BIGINT NOT NULL,
                product_id    BIGINT NOT NULL,
                quantity      INT    NOT NULL,
                CONSTRAINT fk_cartitems_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
                CONSTRAINT fk_cartitems_product FOREIGN KEY (product_id) REFERENCES products(id)
            ) ENGINE=InnoDB
            """,
            """
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
            ) ENGINE=InnoDB
            """,
            """
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
            ) ENGINE=InnoDB
            """
        };

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                stmt.execute(sql);
            }
            System.out.println(">>> Database schema verified/created.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise database schema. Is MySQL running and reachable at " + AppConfig.DB_URL + " ?", e);
        }
    }
}
