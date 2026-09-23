package com.babyshop.handler;

import com.babyshop.dto.request.UpdateProfileRequest;
import com.babyshop.model.User;
import com.babyshop.router.RequestContext;
import com.babyshop.router.Router;
import com.babyshop.service.UserService;

/** Authenticated endpoints for the logged-in user's own profile. */
public class UserHandler {

    private final UserService userService = new UserService();

    public void register(Router router) {
        router.get("/api/users/me", this::getProfile);
        router.put("/api/users/me", this::updateProfile);
    }

    private void getProfile(RequestContext ctx) {
        User user = ctx.requireAuth();
        ctx.json(200, userService.toResponse(user));
    }

    private void updateProfile(RequestContext ctx) {
        User user = ctx.requireAuth();
        UpdateProfileRequest req = ctx.readBody(UpdateProfileRequest.class);
        ctx.json(200, userService.updateProfile(user, req.name, req.phone));
    }
}
