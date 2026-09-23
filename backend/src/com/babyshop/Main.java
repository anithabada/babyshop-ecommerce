package com.babyshop;

import com.babyshop.config.AppConfig;
import com.babyshop.config.DataSeeder;
import com.babyshop.db.Db;
import com.babyshop.handler.*;
import com.babyshop.router.Router;
import com.babyshop.router.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Entry point of the Baby Products E-Commerce backend.
 *
 * This is a plain Java application (no Spring, no Maven): it uses the
 * JDK's built-in com.sun.net.httpserver.HttpServer to expose REST APIs
 * under /api/**, talks to MySQL directly via JDBC, and also serves the
 * static HTML/CSS/JS frontend at "/" so the whole app runs from a single
 * `java` command.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("=================================================");
        System.out.println(" BabyShop Backend - starting up...");
        System.out.println("=================================================");

        // 1. Make sure the database schema exists
        Db.initSchema();

        // 2. Seed a default admin account + sample catalog if empty
        new DataSeeder().run();

        // 3. Build the API router
        Router apiRouter = new Router();
        new AuthHandler().register(apiRouter);
        new ProductHandler().register(apiRouter);
        new CategoryHandler().register(apiRouter);
        new UserHandler().register(apiRouter);
        new CartHandler().register(apiRouter);
        new OrderHandler().register(apiRouter);
        new AdminHandler().register(apiRouter);

        // 4. Start the HTTP server: API under /api, static frontend under /
        HttpServer server = HttpServer.create(new InetSocketAddress(AppConfig.SERVER_PORT), 0);
        server.createContext("/api", apiRouter);
        server.createContext("/", new StaticFileHandler(AppConfig.FRONTEND_DIR));
        server.setExecutor(Executors.newFixedThreadPool(20));
        server.start();

        System.out.println(">>> Server running at http://localhost:" + AppConfig.SERVER_PORT);
        System.out.println(">>> Storefront:        http://localhost:" + AppConfig.SERVER_PORT + "/index.html");
        System.out.println(">>> Admin login:       admin@babyshop.com / Admin@123");
        System.out.println("=================================================");
    }
}
