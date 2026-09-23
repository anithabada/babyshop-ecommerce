package com.babyshop.dto.request;

import java.math.BigDecimal;

public class ProductRequest {
    public String name;
    public String description;
    public BigDecimal price;
    public Integer stockQuantity;
    public String imageUrl;
    public String brand;
    public Long categoryId;
}
