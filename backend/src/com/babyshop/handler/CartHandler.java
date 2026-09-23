package com.babyshop.handler;

import com.babyshop.dto.request.CartItemRequest;
import com.babyshop.dto.request.QuantityRequest;
import com.babyshop.model.User;
import com.babyshop.router.RequestContext;
import com.babyshop.router.Router;
import com.babyshop.service.CartService;

/** Authenticated endpoints for managing the shopping cart. */
public class CartHandler {

    private final CartService cartService = new CartService();

    public void register(Router router) {
        router.get("/api/cart", this::getCart);
        router.post("/api/cart/items", this::addItem);
        router.put("/api/cart/items/{itemId}", this::updateItem);
        router.delete("/api/cart/items/{itemId}", this::removeItem);
    }

    private void getCart(RequestContext ctx) {
        User user = ctx.requireAuth();
        ctx.json(200, cartService.getCart(user));
    }

    private void addItem(RequestContext ctx) {
        User user = ctx.requireAuth();
        CartItemRequest req = ctx.readBody(CartItemRequest.class);
        ctx.json(200, cartService.addToCart(user, req.productId, req.quantity));
    }

    private void updateItem(RequestContext ctx) {
        User user = ctx.requireAuth();
        Long itemId = ctx.pathParamLong("itemId");
        QuantityRequest req = ctx.readBody(QuantityRequest.class);
        ctx.json(200, cartService.updateItemQuantity(user, itemId, req.quantity));
    }

    private void removeItem(RequestContext ctx) {
        User user = ctx.requireAuth();
        Long itemId = ctx.pathParamLong("itemId");
        ctx.json(200, cartService.removeItem(user, itemId));
    }
}
