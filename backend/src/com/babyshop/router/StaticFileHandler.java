package com.babyshop.router;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves the static frontend (HTML/CSS/JS) directly from disk, so the whole
 * app - API + website - can be launched with a single "java" command and
 * no separate web server is required.
 */
public class StaticFileHandler implements HttpHandler {

    private final File rootDir;

    public StaticFileHandler(String rootDirPath) {
        this.rootDir = new File(rootDirPath);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) requestPath = "/index.html";

        File file = new File(rootDir, requestPath).getCanonicalFile();

        // Basic path-traversal protection: served file must stay inside rootDir
        if (!file.getPath().startsWith(rootDir.getCanonicalPath())) {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
            return;
        }

        if (!file.exists() || file.isDirectory()) {
            // Fallback to index.html for unknown paths would be nice for SPA-style
            // routing, but since this is a multi-page static site, 404 is correct.
            byte[] notFound = "404 Not Found".getBytes();
            exchange.sendResponseHeaders(404, notFound.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(notFound); }
            exchange.close();
            return;
        }

        String contentType = guessContentType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, file.length());
        try (OutputStream os = exchange.getResponseBody(); FileInputStream fis = new FileInputStream(file)) {
            fis.transferTo(os);
        }
        exchange.close();
    }

    private String guessContentType(String fileName) {
        if (fileName.endsWith(".html")) return "text/html; charset=utf-8";
        if (fileName.endsWith(".css")) return "text/css; charset=utf-8";
        if (fileName.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (fileName.endsWith(".json")) return "application/json; charset=utf-8";
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) return "image/jpeg";
        if (fileName.endsWith(".svg")) return "image/svg+xml";
        if (fileName.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }
}
