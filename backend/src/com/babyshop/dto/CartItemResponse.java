package com.babyshop.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    public Long id;
    public Long productId;
    public String productName;
    public String imageUrl;
    public BigDecimal unitPrice;
    public Integer quantity;
    public BigDecimal subtotal;
    public Integer availableStock;

    public CartItemResponse(Long id, Long productId, String productName, String imageUrl,
                             BigDecimal unitPrice, Integer quantity, BigDecimal subtotal, Integer availableStock) {
        this.id = id; this.productId = productId; this.productName = productName; this.imageUrl = imageUrl;
        this.unitPrice = unitPrice; this.quantity = quantity; this.subtotal = subtotal; this.availableStock = availableStock;
    }
}
