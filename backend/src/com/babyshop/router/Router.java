package com.babyshop.router;

import com.babyshop.dto.ErrorResponse;
import com.babyshop.exception.ApiException;
import com.babyshop.exception.ValidationException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A tiny hand-rolled HTTP router, playing the same role that Spring MVC's
 * @RequestMapping/@GetMapping/@PostMapping annotations play: it matches an
 * incoming (method, path) pair against a registered pattern like
 * "/api/products/{id}" and extracts path variables such as {id}.
 */
public class Router implements HttpHandler {

    @FunctionalInterface
    public interface Handler {
        void handle(RequestContext ctx) throws Exception;
    }

    private static class Route {
        String method;
        Pattern pattern;
        List<String> paramNames;
        Handler handler;
    }

    private final List<Route> routes = new ArrayList<>();

    public void get(String path, Handler handler) { add("GET", path, handler); }
    public void post(String path, Handler handler) { add("POST", path, handler); }
    public void put(String path, Handler handler) { add("PUT", path, handler); }
    public void delete(String path, Handler handler) { add("DELETE", path, handler); }

    private void add(String method, String path, Handler handler) {
        List<String> paramNames = new ArrayList<>();
        StringBuilder regex = new StringBuilder("^");
        for (String segment : path.split("/")) {
            if (segment.isEmpty()) continue;
            if (segment.startsWith("{") && segment.endsWith("}")) {
                paramNames.add(segment.substring(1, segment.length() - 1));
                regex.append("/([^/]+)");
            } else {
                regex.append("/").append(Pattern.quote(segment));
            }
        }
        regex.append("/?$");

        Route route = new Route();
        route.method = method;
        route.pattern = Pattern.compile(regex.toString());
        route.paramNames = paramNames;
        route.handler = handler;
        routes.add(route);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // Handle CORS preflight
        if ("OPTIONS".equalsIgnoreCase(method)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, PATCH, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        for (Route route : routes) {
            if (!route.method.equalsIgnoreCase(method)) continue;
            Matcher matcher = route.pattern.matcher(path);
            if (!matcher.matches()) continue;

            Map<String, String> params = new LinkedHashMap<>();
            for (int i = 0; i < route.paramNames.size(); i++) {
                params.put(route.paramNames.get(i), matcher.group(i + 1));
            }

            RequestContext ctx = new RequestContext(exchange, params);
            try {
                route.handler.handle(ctx);
            } catch (ValidationException ve) {
                ctx.json(ve.getStatusCode(), new ErrorResponse(ve.getStatusCode(), ve.getMessage(), path, ve.getFieldErrors()));
            } catch (ApiException ae) {
                ctx.json(ae.getStatusCode(), new ErrorResponse(ae.getStatusCode(), ae.getMessage(), path, null));
            } catch (Exception e) {
                e.printStackTrace();
                ctx.json(500, new ErrorResponse(500, "An unexpected error occurred: " + e.getMessage(), path, null));
            } finally {
                exchange.close();
            }
            return;
        }

        // No route matched
        RequestContext ctx = new RequestContext(exchange, Map.of());
        ctx.json(404, new ErrorResponse(404, "No such endpoint: " + method + " " + path, path, null));
        exchange.close();
    }
}
