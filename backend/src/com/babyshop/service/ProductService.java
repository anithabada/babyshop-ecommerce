package com.babyshop.service;

import com.babyshop.dao.CategoryDao;
import com.babyshop.dao.ProductDao;
import com.babyshop.dto.ProductResponse;
import com.babyshop.exception.ApiException;
import com.babyshop.exception.ValidationException;
import com.babyshop.model.Category;
import com.babyshop.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class ProductService {

    private final ProductDao productDao = new ProductDao();
    private final CategoryDao categoryDao = new CategoryDao();

    public List<ProductResponse> getAllActiveProducts() {
        return productDao.findAllActive().stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productDao.findByCategoryActive(categoryId).stream().map(this::toResponse).toList();
    }

    public List<ProductResponse> search(String keyword) {
        return productDao.search(keyword).stream().map(this::toResponse).toList();
    }

    public ProductResponse getProductById(Long id) {
        return toResponse(findEntity(id));
    }

    public Product findEntity(Long id) {
        return productDao.findById(id)
                .orElseThrow(() -> ApiException.notFound("Product not found with id: " + id));
    }

    public List<ProductResponse> getAllProductsAdmin() {
        return productDao.findAllAdmin().stream().map(this::toResponse).toList();
    }

    public ProductResponse createProduct(String name, String description, BigDecimal price, Integer stockQuantity,
                                          String imageUrl, String brand, Long categoryId) {
        validate(name, price, stockQuantity, brand, categoryId);
        Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> ApiException.badRequest("Invalid category selected"));

        Product product = new Product();
        product.setName(name.trim());
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);
        product.setBrand(brand.trim());
        product.setCategoryId(category.getId());
        product.setActive(true);

        Product saved = productDao.save(product);
        saved.setCategoryName(category.getName());
        return toResponse(saved);
    }

    public ProductResponse updateProduct(Long id, String name, String description, BigDecimal price, Integer stockQuantity,
                                          String imageUrl, String brand, Long categoryId) {
        validate(name, price, stockQuantity, brand, categoryId);
        Product product = findEntity(id);
        Category category = categoryDao.findById(categoryId)
                .orElseThrow(() -> ApiException.badRequest("Invalid category selected"));

        product.setName(name.trim());
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);
        product.setBrand(brand.trim());
        product.setCategoryId(category.getId());

        productDao.update(product);
        product.setCategoryName(category.getName());
        return toResponse(product);
    }

    /** Soft delete: mark inactive so historical orders referencing it stay intact. */
    public void deleteProduct(Long id) {
        Product product = findEntity(id);
        product.setActive(false);
        productDao.update(product);
    }

    private void validate(String name, BigDecimal price, Integer stockQuantity, String brand, Long categoryId) {
        ValidationException.Builder v = new ValidationException.Builder();
        v.require("name", name, "Product name is required");
        v.check("price", price != null && price.compareTo(BigDecimal.ZERO) > 0, "Price must be greater than 0");
        v.check("stockQuantity", stockQuantity != null && stockQuantity >= 0, "Stock quantity cannot be negative");
        v.require("brand", brand, "Brand is required");
        v.check("categoryId", categoryId != null, "Category is required");
        v.throwIfInvalid();
    }

    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStockQuantity(),
                p.getImageUrl(), p.getBrand(), p.getCategoryId(), p.getCategoryName(), p.isActive()
        );
    }
}
