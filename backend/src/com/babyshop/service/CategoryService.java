package com.babyshop.service;

import com.babyshop.dao.CategoryDao;
import com.babyshop.dto.CategoryResponse;
import com.babyshop.exception.ApiException;
import com.babyshop.exception.ValidationException;
import com.babyshop.model.Category;

import java.util.List;

public class CategoryService {

    private final CategoryDao categoryDao = new CategoryDao();

    public List<CategoryResponse> getAllCategories() {
        return categoryDao.findAll().stream().map(this::toResponse).toList();
    }

    public CategoryResponse getCategoryById(Long id) {
        return toResponse(findEntity(id));
    }

    public Category findEntity(Long id) {
        return categoryDao.findById(id)
                .orElseThrow(() -> ApiException.notFound("Category not found with id: " + id));
    }

    public CategoryResponse createCategory(String name, String description) {
        ValidationException.Builder v = new ValidationException.Builder();
        v.require("name", name, "Category name is required");
        v.throwIfInvalid();

        if (categoryDao.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("Category already exists: " + name);
        }
        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description);
        return toResponse(categoryDao.save(category));
    }

    public CategoryResponse updateCategory(Long id, String name, String description) {
        ValidationException.Builder v = new ValidationException.Builder();
        v.require("name", name, "Category name is required");
        v.throwIfInvalid();

        Category category = findEntity(id);
        category.setName(name.trim());
        category.setDescription(description);
        categoryDao.update(category);
        return toResponse(category);
    }

    public void deleteCategory(Long id) {
        findEntity(id); // ensures 404 if missing
        categoryDao.delete(id);
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription());
    }
}
