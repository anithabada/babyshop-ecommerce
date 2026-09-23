package com.babyshop.dto;

import java.math.BigDecimal;

public class ProductResponse {
    public Long id;
    public String name;
    public String description;
    public BigDecimal price;
    public Integer stockQuantity;
    public String imageUrl;
    public String brand;
    public Long categoryId;
    public String categoryName;
    public boolean active;

    public ProductResponse(Long id, String name, String description, BigDecimal price, Integer stockQuantity,
                            String imageUrl, String brand, Long categoryId, String categoryName, boolean active) {
        this.id = id; this.name = name; this.description = description; this.price = price;
        this.stockQuantity = stockQuantity; this.imageUrl = imageUrl; this.brand = brand;
        this.categoryId = categoryId; this.categoryName = categoryName; this.active = active;
    }
}
