package com.babyshop.handler;

import com.babyshop.router.RequestContext;
import com.babyshop.router.Router;
import com.babyshop.service.CategoryService;

/** Public, read-only endpoints for browsing categories. */
public class CategoryHandler {

    private final CategoryService categoryService = new CategoryService();

    public void register(Router router) {
        router.get("/api/categories", ctx -> ctx.json(200, categoryService.getAllCategories()));
        router.get("/api/categories/{id}", ctx -> ctx.json(200, categoryService.getCategoryById(ctx.pathParamLong("id"))));
    }
}
