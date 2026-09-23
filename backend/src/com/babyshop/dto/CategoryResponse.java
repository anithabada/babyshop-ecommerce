package com.babyshop.dto;

public class CategoryResponse {
    public Long id;
    public String name;
    public String description;

    public CategoryResponse(Long id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }
}
