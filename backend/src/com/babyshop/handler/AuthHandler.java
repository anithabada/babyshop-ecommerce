package com.babyshop.handler;

import com.babyshop.dto.AuthResponse;
import com.babyshop.dto.request.LoginRequest;
import com.babyshop.dto.request.RegisterRequest;
import com.babyshop.router.RequestContext;
import com.babyshop.router.Router;
import com.babyshop.service.AuthService;

/** Public authentication endpoints: /api/auth/register and /api/auth/login */
public class AuthHandler {

    private final AuthService authService = new AuthService();

    public void register(Router router) {
        router.post("/api/auth/register", this::handleRegister);
        router.post("/api/auth/login", this::handleLogin);
    }

    private void handleRegister(RequestContext ctx) {
        RegisterRequest req = ctx.readBody(RegisterRequest.class);
        AuthResponse response = authService.register(req.name, req.email, req.phone, req.password);
        ctx.json(201, response);
    }

    private void handleLogin(RequestContext ctx) {
        LoginRequest req = ctx.readBody(LoginRequest.class);
        AuthResponse response = authService.login(req.email, req.password);
        ctx.json(200, response);
    }
}
