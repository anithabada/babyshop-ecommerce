package com.babyshop.dao;

import com.babyshop.db.Db;
import com.babyshop.model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductDao {

    private static final String BASE_SELECT =
        "SELECT p.*, c.name AS category_name FROM products p JOIN categories c ON p.category_id = c.id ";

    public List<Product> findAllActive() {
        return query(BASE_SELECT + "WHERE p.active = 1 ORDER BY p.created_at DESC");
    }

    public List<Product> findAllAdmin() {
        return query(BASE_SELECT + "ORDER BY p.created_at DESC");
    }

    public List<Product> findByCategoryActive(Long categoryId) {
        String sql = BASE_SELECT + "WHERE p.active = 1 AND p.category_id = ? ORDER BY p.created_at DESC";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            return runQuery(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching products by category", e);
        }
    }

    public List<Product> search(String keyword) {
        String sql = BASE_SELECT + "WHERE p.active = 1 AND " +
                "(LOWER(p.name) LIKE ? OR LOWER(p.brand) LIKE ? OR LOWER(c.name) LIKE ?) " +
                "ORDER BY p.created_at DESC";
        String like = "%" + keyword.toLowerCase() + "%";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            return runQuery(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error searching products", e);
        }
    }

    public Optional<Product> findById(Long id) {
        String sql = BASE_SELECT + "WHERE p.id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching product", e);
        }
    }

    public Product save(Product p) {
        String sql = "INSERT INTO products (name, description, price, stock_quantity, image_url, brand, category_id, active, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStockQuantity());
            ps.setString(5, p.getImageUrl());
            ps.setString(6, p.getBrand());
            ps.setLong(7, p.getCategoryId());
            ps.setBoolean(8, p.isActive());
            ps.setTimestamp(9, Timestamp.valueOf(now));
            ps.setTimestamp(10, Timestamp.valueOf(now));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getLong(1));
            }
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving product", e);
        }
    }

    public void update(Product p) {
        String sql = "UPDATE products SET name=?, description=?, price=?, stock_quantity=?, image_url=?, brand=?, category_id=?, active=?, updated_at=? WHERE id=?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStockQuantity());
            ps.setString(5, p.getImageUrl());
            ps.setString(6, p.getBrand());
            ps.setLong(7, p.getCategoryId());
            ps.setBoolean(8, p.isActive());
            ps.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(10, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        }
    }

    /** Just decrements stock (used during checkout, inside the same connection ideally, but kept simple here). */
    public void updateStock(Long productId, int newStock, Connection conn) throws SQLException {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newStock);
            ps.setLong(2, productId);
            ps.executeUpdate();
        }
    }

    private List<Product> query(String sql) {
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return runQuery(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching products", e);
        }
    }

    private List<Product> runQuery(PreparedStatement ps) throws SQLException {
        List<Product> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        p.setImageUrl(rs.getString("image_url"));
        p.setBrand(rs.getString("brand"));
        p.setCategoryId(rs.getLong("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setActive(rs.getBoolean("active"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) p.setUpdatedAt(updated.toLocalDateTime());
        return p;
    }
}
