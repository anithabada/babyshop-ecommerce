package com.babyshop.dao;

import com.babyshop.db.Db;
import com.babyshop.model.Cart;
import com.babyshop.model.CartItem;

import java.sql.*;
import java.util.Optional;

public class CartDao {

    /** Finds (or lazily creates) the cart belonging to a user, with its items populated. */
    public Cart findOrCreateByUserId(Long userId) {
        Optional<Long> cartId = findCartId(userId);
        long id = cartId.orElseGet(() -> createCart(userId));
        return loadCartWithItems(id, userId);
    }

    private Optional<Long> findCartId(Long userId) {
        String sql = "SELECT id FROM cart WHERE user_id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cart", e);
        }
    }

    private long createCart(Long userId) {
        String sql = "INSERT INTO cart (user_id) VALUES (?)";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating cart", e);
        }
    }

    private Cart loadCartWithItems(long cartId, Long userId) {
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUserId(userId);

        String sql = "SELECT ci.id, ci.product_id, ci.quantity, p.name AS product_name, p.image_url, " +
                "p.price, p.stock_quantity FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                "WHERE ci.cart_id = ? ORDER BY ci.id";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setId(rs.getLong("id"));
                    item.setCartId(cartId);
                    item.setProductId(rs.getLong("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setImageUrl(rs.getString("image_url"));
                    item.setUnitPrice(rs.getBigDecimal("price"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setAvailableStock(rs.getInt("stock_quantity"));
                    cart.getItems().add(item);
                }
            }
            return cart;
        } catch (SQLException e) {
            throw new RuntimeException("Error loading cart items", e);
        }
    }

    public Optional<Long> findItemQuantity(Long cartId, Long productId) {
        String sql = "SELECT id FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            ps.setLong(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking cart item", e);
        }
    }

    public void insertItem(Long cartId, Long productId, int quantity) {
        String sql = "INSERT INTO cart_items (cart_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting cart item", e);
        }
    }

    public void updateItemQuantity(Long cartItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating cart item", e);
        }
    }

    public void deleteItem(Long cartItemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting cart item", e);
        }
    }

    public void clearCart(Long cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = Db.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cartId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing cart", e);
        }
    }

    /** Clears the cart items using an existing connection (for use inside the checkout transaction). */
    public void clearCartInTransaction(Long cartId, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM cart_items WHERE cart_id = ?")) {
            ps.setLong(1, cartId);
            ps.executeUpdate();
        }
    }
}
