package com.babyshop.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {
    public Long cartId;
    public List<CartItemResponse> items;
    public BigDecimal totalAmount;
    public Integer totalItems;

    public CartResponse(Long cartId, List<CartItemResponse> items, BigDecimal totalAmount, Integer totalItems) {
        this.cartId = cartId; this.items = items; this.totalAmount = totalAmount; this.totalItems = totalItems;
    }
}
