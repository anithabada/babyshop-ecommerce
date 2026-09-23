package com.babyshop.router;

import com.babyshop.dao.UserDao;
import com.babyshop.exception.ApiException;
import com.babyshop.model.Role;
import com.babyshop.model.User;
import com.babyshop.util.JsonUtil;
import com.babyshop.util.JwtUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a single incoming HTTP request/response, providing convenience
 * methods that mimic what @RequestBody, @PathVariable, @RequestParam and
 * Spring Security's Authentication would give you in a Spring controller.
 */
public class RequestContext {

    private final HttpExchange exchange;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;
    private String cachedBody;
    private static final UserDao userDao = new UserDao();

    public RequestContext(HttpExchange exchange, Map<String, String> pathParams) {
        this.exchange = exchange;
        this.pathParams = pathParams;
        this.queryParams = parseQuery(exchange.getRequestURI().getRawQuery());
    }

    public String pathParam(String name) {
        return pathParams.get(name);
    }

    public Long pathParamLong(String name) {
        try {
            return Long.parseLong(pathParams.get(name));
        } catch (Exception e) {
            throw ApiException.badRequest("Invalid id: " + pathParams.get(name));
        }
    }

    public String queryParam(String name) {
        return queryParams.get(name);
    }

    public <T> T readBody(Class<T> clazz) {
        String body = readRawBody();
        if (body == null || body.isBlank()) {
            throw ApiException.badRequest("Request body is required");
        }
        try {
            T obj = JsonUtil.GSON.fromJson(body, clazz);
            if (obj == null) throw ApiException.badRequest("Request body is required");
            return obj;
        } catch (Exception e) {
            throw ApiException.badRequest("Malformed JSON request body");
        }
    }

    private String readRawBody() {
        if (cachedBody != null) return cachedBody;
        try (InputStream is = exchange.getRequestBody()) {
            cachedBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return cachedBody;
        } catch (IOException e) {
            throw new RuntimeException("Error reading request body", e);
        }
    }

    /** Resolves the logged-in User from the Authorization: Bearer token header. Throws 401 if absent/invalid. */
    public User requireAuth() {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw ApiException.unauthorized("Missing or invalid Authorization header");
        }
        String token = header.substring(7);
        String email = JwtUtil.validateAndGetSubject(token);
        if (email == null) {
            throw ApiException.unauthorized("Invalid or expired token");
        }
        return userDao.findByEmail(email)
                .orElseThrow(() -> ApiException.unauthorized("User no longer exists"));
    }

    /** Same as requireAuth() but also enforces ROLE_ADMIN. */
    public User requireAdmin() {
        User user = requireAuth();
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw ApiException.forbidden("You do not have permission to perform this action");
        }
        return user;
    }

    public void json(int status, Object body) {
        try {
            String json = JsonUtil.GSON.toJson(body);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            addCorsHeaders();
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing response", e);
        }
    }

    public void addCorsHeaders() {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    public HttpExchange raw() {
        return exchange;
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) return map;
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            try {
                String key = java.net.URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String value = java.net.URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                map.put(key, value);
            } catch (Exception ignored) {}
        }
        return map;
    }
}
