package com.babyshop.dao;

import com.babyshop.db.Db;
import com.babyshop.model.*;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderDao {

    /**
     * Places an order from the given cart in a single JDBC transaction:
     * validates stock, decrements it, inserts order + order_items, empties the cart.
     * If anything fails, the whole transaction is rolled back - no partial state.
     */
    public Order checkout(Cart cart, Long userId, String shippingAddress, String contactPhone) throws SQLException {
        Connection conn = null;
        try {
            conn = Db.getConnection();
            conn.setAutoCommit(false);

            BigDecimal total = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            // 1. Re-check stock for every item (row-level lock via SELECT ... FOR UPDATE)
            for (CartItem item : cart.getItems()) {
                int currentStock;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT stock_quantity, name, price FROM products WHERE id = ? FOR UPDATE")) {
                    ps.setLong(1, item.getProductId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new com.babyshop.exception.ApiException(400, "Product no longer exists: " + item.getProductName());
                        }
                        currentStock = rs.getInt("stock_quantity");
                        item.setProductName(rs.getString("name"));
                        item.setUnitPrice(rs.getBigDecimal("price"));
                    }
                }

                if (item.getQuantity() > currentStock) {
                    throw new com.babyshop.exception.ApiException(400,
                            "Insufficient stock for \"" + item.getProductName() + "\". Only " + currentStock + " left.");
                }

                // 2. Decrement stock
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE products SET stock_quantity = stock_quantity - ? WHERE id = ?")) {
                    ps.setInt(1, item.getQuantity());
                    ps.setLong(2, item.getProductId());
                    ps.executeUpdate();
                }

                BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                total = total.add(subtotal);

                OrderItem oi = new OrderItem();
                oi.setProductId(item.getProductId());
                oi.setProductName(item.getProductName());
                oi.setUnitPrice(item.getUnitPrice());
                oi.setQuantity(item.getQuantity());
                oi.setSubtotal(subtotal);
                orderItems.add(oi);
            }

            // 3. Insert order
            LocalDateTime now = LocalDateTime.now();
            long orderId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO orders (user_id, total_amount, status, shipping_address, contact_phone, created_at, updated_at) " +
                    "VALUES (?, ?, 'PENDING', ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, userId);
                ps.setBigDecimal(2, total);
                ps.setString(3, shippingAddress);
                ps.setString(4, contactPhone);
                ps.setTimestamp(5, Timestamp.valueOf(now));
                ps.setTimestamp(6, Timestamp.valueOf(now));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    orderId = keys.getLong(1);
                }
            }

            // 4. Insert order_items
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO order_items (order_id, product_id, product_name, unit_price, quantity, subtotal) VALUES (?, ?, ?, ?, ?, ?)")) {
                for (OrderItem oi : orderItems) {
                    ps.setLong(1, orderId);
                    ps.setLong(2, oi.getProductId());
                    ps.setString(3, oi.getProductName());
                    ps.setBigDecimal(4, oi.getUnitPrice());
                    ps.setInt(5, oi.getQuantity());
                    ps.setBigDecimal(6, oi.getSubtotal());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 5. Empty the cart
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
                ps.setLong(1, cart.getId());
                ps.executeUpdate();
            }

            conn.commit();

            Order order = new Order();
            order.setId(orderId);
            order.setUserId(userId);
            order.setTotalAmount(total);
            order.setStatus(OrderStatus.PENDING);
            order.setShippingAddress(shippingAddress);
            order.setContactPhone(contactPhone);
            order.setItems(orderItems);
            order.setCreatedAt(now);
            order.setUpdatedAt(now);
            return order;

        } catch (Exception e) {
            if (conn != null) conn.rollback();
            if (e instanceof com.babyshop.exception.ApiException apiEx) throw apiEx;
            throw new RuntimeException("Checkout failed", e);
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public List<Order> findByUserId(Long userId) {
        String sql = "SELECT o.*, u.name AS user_name FROM orders o JOIN users u ON o.user_id = u.id " +
                "WHERE o.user_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return mapList(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching orders", e);
        }
    }

    public List<Order> findAll() {
        String sql = "SELECT o.*, u.name AS user_name FROM orders o JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            return mapList(ps);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all orders", e);
        }
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT o.*, u.name AS user_name FROM orders o JOIN users u ON o.user_id = u.id WHERE o.id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            List<Order> list = mapList(ps);
            return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching order", e);
        }
    }

    public void updateStatus(Long orderId, OrderStatus status) {
        String sql = "UPDATE orders SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(3, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating order status", e);
        }
    }

    private List<Order> mapList(PreparedStatement ps) throws SQLException {
        List<Order> orders = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getLong("id"));
                o.setUserId(rs.getLong("user_id"));
                o.setUserName(rs.getString("user_name"));
                o.setTotalAmount(rs.getBigDecimal("total_amount"));
                o.setStatus(OrderStatus.valueOf(rs.getString("status")));
                o.setShippingAddress(rs.getString("shipping_address"));
                o.setContactPhone(rs.getString("contact_phone"));
                o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                Timestamp updated = rs.getTimestamp("updated_at");
                if (updated != null) o.setUpdatedAt(updated.toLocalDateTime());
                orders.add(o);
            }
        }
        // populate items for each order (simple N+1 - fine at this scale)
        for (Order o : orders) {
            o.setItems(findItemsByOrderId(o.getId()));
        }
        return orders;
    }

    private List<OrderItem> findItemsByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_items WHERE order_id = ? ORDER BY id";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            List<OrderItem> items = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getLong("id"));
                    oi.setOrderId(orderId);
                    long pid = rs.getLong("product_id");
                    oi.setProductId(rs.wasNull() ? null : pid);
                    oi.setProductName(rs.getString("product_name"));
                    oi.setUnitPrice(rs.getBigDecimal("unit_price"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setSubtotal(rs.getBigDecimal("subtotal"));
                    items.add(oi);
                }
            }
            return items;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching order items", e);
        }
    }
}
