package com.babyshop.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    public Long id;
    public Long userId;
    public String userName;
    public BigDecimal totalAmount;
    public String status;
    public String shippingAddress;
    public String contactPhone;
    public List<OrderItemResponse> items;
    public LocalDateTime createdAt;

    public OrderResponse(Long id, Long userId, String userName, BigDecimal totalAmount, String status,
                          String shippingAddress, String contactPhone, List<OrderItemResponse> items, LocalDateTime createdAt) {
        this.id = id; this.userId = userId; this.userName = userName; this.totalAmount = totalAmount;
        this.status = status; this.shippingAddress = shippingAddress; this.contactPhone = contactPhone;
        this.items = items; this.createdAt = createdAt;
    }
}
