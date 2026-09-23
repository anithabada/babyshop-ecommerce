package com.babyshop.dto;

import java.math.BigDecimal;

public class OrderItemResponse {
    public Long productId;
    public String productName;
    public BigDecimal unitPrice;
    public Integer quantity;
    public BigDecimal subtotal;

    public OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, Integer quantity, BigDecimal subtotal) {
        this.productId = productId; this.productName = productName; this.unitPrice = unitPrice;
        this.quantity = quantity; this.subtotal = subtotal;
    }
}
