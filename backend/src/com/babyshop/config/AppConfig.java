package com.babyshop.config;

/**
 * Centralised application configuration.
 * In a real production app these would come from environment variables;
 * here they are simple constants so the project is easy to run and inspect.
 */
public final class AppConfig {

    // ----- Server -----
    public static final int SERVER_PORT =
        Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

    // ----- Database -----
    public static final String DB_URL = "jdbc:mariadb://localhost:3306/babyshop_db?useSSL=false&allowPublicKeyRetrieval=true";
    public static final String DB_USER = "babyshop_user";
    public static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    // ----- JWT -----
    public static final String JWT_SECRET =  System.getenv("JWT_SECRET");
    public static final long JWT_EXPIRATION_MS = 24L * 60 * 60 * 1000; // 24 hours

    // ----- Static frontend -----
    // Directory containing index.html, css/, js/ - served at "/"
    public static final String FRONTEND_DIR = "../frontend";

    private AppConfig() {}
}
