package com.babyshop.handler;

import com.babyshop.dto.MessageResponse;
import com.babyshop.dto.request.CategoryRequest;
import com.babyshop.dto.request.ProductRequest;
import com.babyshop.dto.request.UpdateOrderStatusRequest;
import com.babyshop.router.RequestContext;
import com.babyshop.router.Router;
import com.babyshop.service.CategoryService;
import com.babyshop.service.OrderService;
import com.babyshop.service.ProductService;
import com.babyshop.service.UserService;

/**
 * All admin-only endpoints live under /api/admin/**.
 * Every handler calls ctx.requireAdmin() first, which enforces both
 * authentication (valid JWT) and authorization (ROLE_ADMIN) - the
 * equivalent of Spring Security's hasRole("ADMIN") rule.
 */
public class AdminHandler {

    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    private final UserService userService = new UserService();
    private final OrderService orderService = new OrderService();

    public void register(Router router) {
        // Products
        router.get("/api/admin/products", ctx -> { ctx.requireAdmin(); ctx.json(200, productService.getAllProductsAdmin()); });
        router.post("/api/admin/products", this::createProduct);
        router.put("/api/admin/products/{id}", this::updateProduct);
        router.delete("/api/admin/products/{id}", this::deleteProduct);

        // Categories
        router.post("/api/admin/categories", this::createCategory);
        router.put("/api/admin/categories/{id}", this::updateCategory);
        router.delete("/api/admin/categories/{id}", this::deleteCategory);

        // Users
        router.get("/api/admin/users", ctx -> { ctx.requireAdmin(); ctx.json(200, userService.getAllUsers()); });

        // Orders
        router.get("/api/admin/orders", ctx -> { ctx.requireAdmin(); ctx.json(200, orderService.getAllOrders()); });
        router.put("/api/admin/orders/{id}/status", this::updateOrderStatus);
    }

    private void createProduct(RequestContext ctx) {
        ctx.requireAdmin();
        ProductRequest req = ctx.readBody(ProductRequest.class);
        ctx.json(201, productService.createProduct(req.name, req.description, req.price, req.stockQuantity, req.imageUrl, req.brand, req.categoryId));
    }

    private void updateProduct(RequestContext ctx) {
        ctx.requireAdmin();
        Long id = ctx.pathParamLong("id");
        ProductRequest req = ctx.readBody(ProductRequest.class);
        ctx.json(200, productService.updateProduct(id, req.name, req.description, req.price, req.stockQuantity, req.imageUrl, req.brand, req.categoryId));
    }

    private void deleteProduct(RequestContext ctx) {
        ctx.requireAdmin();
        Long id = ctx.pathParamLong("id");
        productService.deleteProduct(id);
        ctx.json(200, new MessageResponse("Product deleted successfully"));
    }

    private void createCategory(RequestContext ctx) {
        ctx.requireAdmin();
        CategoryRequest req = ctx.readBody(CategoryRequest.class);
        ctx.json(201, categoryService.createCategory(req.name, req.description));
    }

    private void updateCategory(RequestContext ctx) {
        ctx.requireAdmin();
        Long id = ctx.pathParamLong("id");
        CategoryRequest req = ctx.readBody(CategoryRequest.class);
        ctx.json(200, categoryService.updateCategory(id, req.name, req.description));
    }

    private void deleteCategory(RequestContext ctx) {
        ctx.requireAdmin();
        Long id = ctx.pathParamLong("id");
        categoryService.deleteCategory(id);
        ctx.json(200, new MessageResponse("Category deleted successfully"));
    }

    private void updateOrderStatus(RequestContext ctx) {
        ctx.requireAdmin();
        Long id = ctx.pathParamLong("id");
        UpdateOrderStatusRequest req = ctx.readBody(UpdateOrderStatusRequest.class);
        ctx.json(200, orderService.updateOrderStatus(id, req.status));
    }
}
