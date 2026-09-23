package com.babyshop.service;

import com.babyshop.dao.CartDao;
import com.babyshop.dto.CartItemResponse;
import com.babyshop.dto.CartResponse;
import com.babyshop.exception.ApiException;
import com.babyshop.model.Cart;
import com.babyshop.model.CartItem;
import com.babyshop.model.Product;
import com.babyshop.model.User;

import java.util.List;
import java.util.Optional;

public class CartService {

    private final CartDao cartDao = new CartDao();
    private final ProductService productService = new ProductService();

    public Cart getOrCreateCart(User user) {
        return cartDao.findOrCreateByUserId(user.getId());
    }

    public CartResponse getCart(User user) {
        return toResponse(getOrCreateCart(user));
    }

    public CartResponse addToCart(User user, Long productId, Integer quantity) {
        if (productId == null) throw ApiException.badRequest("Product id is required");
        if (quantity == null || quantity < 1) throw ApiException.badRequest("Quantity must be at least 1");

        Cart cart = getOrCreateCart(user);
        Product product = productService.findEntity(productId);

        if (!product.isActive()) {
            throw ApiException.badRequest("This product is no longer available");
        }

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId)).findFirst();

        int desiredQty = (existing.isPresent() ? existing.get().getQuantity() : 0) + quantity;
        if (desiredQty > product.getStockQuantity()) {
            throw ApiException.badRequest("Only " + product.getStockQuantity() + " units of \"" + product.getName() + "\" are in stock");
        }

        if (existing.isPresent()) {
            cartDao.updateItemQuantity(existing.get().getId(), desiredQty);
        } else {
            cartDao.insertItem(cart.getId(), productId, quantity);
        }

        return toResponse(cartDao.findOrCreateByUserId(user.getId()));
    }

    public CartResponse updateItemQuantity(User user, Long cartItemId, Integer quantity) {
        if (quantity == null || quantity < 1) throw ApiException.badRequest("Quantity must be at least 1");

        Cart cart = getOrCreateCart(user);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest("Cart item not found"));

        if (quantity > item.getAvailableStock()) {
            throw ApiException.badRequest("Only " + item.getAvailableStock() + " units of \"" + item.getProductName() + "\" are in stock");
        }

        cartDao.updateItemQuantity(cartItemId, quantity);
        return toResponse(cartDao.findOrCreateByUserId(user.getId()));
    }

    public CartResponse removeItem(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);
        boolean exists = cart.getItems().stream().anyMatch(i -> i.getId().equals(cartItemId));
        if (!exists) throw ApiException.badRequest("Cart item not found");

        cartDao.deleteItem(cartItemId);
        return toResponse(cartDao.findOrCreateByUserId(user.getId()));
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(), i.getProductId(), i.getProductName(), i.getImageUrl(),
                        i.getUnitPrice(), i.getQuantity(), i.getSubtotal(), i.getAvailableStock()
                )).toList();
        return new CartResponse(cart.getId(), items, cart.getTotalAmount(), cart.getTotalItems());
    }
}
