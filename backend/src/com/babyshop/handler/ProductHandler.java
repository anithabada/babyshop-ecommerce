package com.babyshop.handler;

import com.babyshop.router.RequestContext;
import com.babyshop.router.Router;
import com.babyshop.service.ProductService;

/** Public, read-only endpoints for browsing/searching the product catalog. */
public class ProductHandler {

    private final ProductService productService = new ProductService();

    public void register(Router router) {
        router.get("/api/products", this::list);
        router.get("/api/products/{id}", this::getOne);
    }

    private void list(RequestContext ctx) {
        String keyword = ctx.queryParam("keyword");
        String categoryId = ctx.queryParam("categoryId");

        if (keyword != null && !keyword.isBlank()) {
            ctx.json(200, productService.search(keyword.trim()));
        } else if (categoryId != null) {
            ctx.json(200, productService.getProductsByCategory(Long.parseLong(categoryId)));
        } else {
            ctx.json(200, productService.getAllActiveProducts());
        }
    }

    private void getOne(RequestContext ctx) {
        Long id = ctx.pathParamLong("id");
        ctx.json(200, productService.getProductById(id));
    }
}
