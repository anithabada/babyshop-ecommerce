package com.babyshop.service;

import com.babyshop.dao.OrderDao;
import com.babyshop.dto.OrderItemResponse;
import com.babyshop.dto.OrderResponse;
import com.babyshop.exception.ApiException;
import com.babyshop.exception.ValidationException;
import com.babyshop.model.Cart;
import com.babyshop.model.Order;
import com.babyshop.model.OrderStatus;
import com.babyshop.model.User;

import java.sql.SQLException;
import java.util.List;

public class OrderService {

    private final OrderDao orderDao = new OrderDao();
    private final CartService cartService = new CartService();

    /**
     * Converts the user's current cart into a placed order. All of this
     * happens in a single DB transaction inside OrderDao.checkout(): if
     * anything fails (e.g. insufficient stock), nothing is committed.
     */
    public OrderResponse checkout(User user, String shippingAddress, String contactPhone) {
        ValidationException.Builder v = new ValidationException.Builder();
        v.require("shippingAddress", shippingAddress, "Shipping address is required");
        v.require("contactPhone", contactPhone, "Contact phone is required");
        v.throwIfInvalid();

        Cart cart = cartService.getOrCreateCart(user);
        if (cart.getItems().isEmpty()) {
            throw ApiException.badRequest("Your cart is empty");
        }

        try {
            Order order = orderDao.checkout(cart, user.getId(), shippingAddress.trim(), contactPhone.trim());
            order.setUserName(user.getName());
            return toResponse(order);
        } catch (SQLException e) {
            throw new RuntimeException("Checkout failed due to a database error", e);
        }
    }

    public List<OrderResponse> getOrderHistory(User user) {
        return orderDao.findByUserId(user.getId()).stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrderById(User user, Long orderId, boolean isAdmin) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found with id: " + orderId));

        if (!isAdmin && !order.getUserId().equals(user.getId())) {
            throw ApiException.forbidden("You do not have access to this order");
        }
        return toResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderDao.findAll().stream().map(this::toResponse).toList();
    }

    public OrderResponse updateOrderStatus(Long orderId, String statusStr) {
        OrderStatus status;
        try {
            status = OrderStatus.valueOf(statusStr);
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid status: " + statusStr);
        }

        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> ApiException.notFound("Order not found with id: " + orderId));

        orderDao.updateStatus(orderId, status);
        order.setStatus(status);
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getProductName(), i.getUnitPrice(), i.getQuantity(), i.getSubtotal()))
                .toList();

        return new OrderResponse(
                order.getId(), order.getUserId(), order.getUserName(), order.getTotalAmount(),
                order.getStatus().name(), order.getShippingAddress(), order.getContactPhone(),
                items, order.getCreatedAt()
        );
    }
}
