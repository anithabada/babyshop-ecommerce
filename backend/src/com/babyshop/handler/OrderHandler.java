package com.babyshop.handler;

import com.babyshop.dto.request.CheckoutRequest;
import com.babyshop.model.Role;
import com.babyshop.model.User;
import com.babyshop.router.RequestContext;
import com.babyshop.router.Router;
import com.babyshop.service.OrderService;

/** Authenticated endpoints for placing orders and viewing order history. */
public class OrderHandler {

    private final OrderService orderService = new OrderService();

    public void register(Router router) {
        router.post("/api/orders/checkout", this::checkout);
        router.get("/api/orders", this::getMyOrders);
        router.get("/api/orders/{id}", this::getOrder);
    }

    private void checkout(RequestContext ctx) {
        User user = ctx.requireAuth();
        CheckoutRequest req = ctx.readBody(CheckoutRequest.class);
        ctx.json(201, orderService.checkout(user, req.shippingAddress, req.contactPhone));
    }

    private void getMyOrders(RequestContext ctx) {
        User user = ctx.requireAuth();
        ctx.json(200, orderService.getOrderHistory(user));
    }

    private void getOrder(RequestContext ctx) {
        User user = ctx.requireAuth();
        Long id = ctx.pathParamLong("id");
        boolean isAdmin = user.getRole() == Role.ROLE_ADMIN;
        ctx.json(200, orderService.getOrderById(user, id, isAdmin));
    }
}
