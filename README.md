# BabyShop — Baby Products E-Commerce Web Application

A complete, working full-stack e-commerce application for baby products:
registration/login, product browsing & search, cart, checkout, order
history, user profile, and a full admin dashboard (products, categories,
orders, users).

> **A note on the tech stack.** The original spec asked for a Spring Boot
> backend built with Maven. This project was developed inside a sandboxed
> environment that has no network access to Maven Central, so Spring Boot's
> dependencies could not be downloaded there. At the requester's direction,
> the backend was instead built as **plain Java** — no framework, no build
> tool — using only the JDK's built-in `HttpServer` for REST APIs, raw JDBC
> for MySQL access, and two small, standard libraries (Gson for JSON,
> jBCrypt for password hashing). It compiles with `javac` and runs with
> `java`, nothing else required. Every architectural piece a Spring Boot app
> would have (controllers, services, repositories/DAOs, DTOs, security
> filter, validation, centralised error handling) is still present — it's
> just hand-wired instead of annotation-driven. If you'd like a genuine
> Spring Boot + Maven version, it is a very mechanical port from this code
> (see "Porting to Spring Boot" at the end) and will work immediately on
> any machine with normal internet access.

---

## 1. Technology Stack

| Layer          | Technology                                                        |
|----------------|---------------------------------------------------------------------|
| Frontend       | HTML5, CSS3, vanilla JavaScript (`fetch` API, no frameworks)       |
| Backend        | Plain Java 17+ (`com.sun.net.httpserver.HttpServer`)                |
| Database       | MySQL 8                                                              |
| DB connectivity| Raw JDBC (MariaDB Connector/J — fully MySQL-protocol compatible)    |
| Auth           | Hand-rolled JWT (HMAC-SHA256) + jBCrypt password hashing            |
| JSON           | Gson                                                                 |

No Maven, Gradle, or Spring Boot is used anywhere in this project.

---

## 2. Project Structure

```
babyshop/
├── backend/
│   ├── src/com/babyshop/
│   │   ├── Main.java                 # entry point: starts HTTP server, wires routes
│   │   ├── config/                   # AppConfig (settings), DataSeeder (demo data)
│   │   ├── db/                       # Db.java — JDBC connection + schema creation
│   │   ├── model/                    # Plain entity classes (User, Product, Order, ...)
│   │   ├── dto/                      # Response shapes sent to the frontend
│   │   ├── dto/request/              # Request body shapes parsed from the frontend
│   │   ├── dao/                      # Data-access layer — hand-written SQL (JDBC)
│   │   ├── service/                  # Business logic (validation, orchestration)
│   │   ├── handler/                  # REST "controllers" (map HTTP routes to services)
│   │   ├── router/                   # Tiny router + RequestContext + static file server
│   │   ├── exception/                # ApiException / ValidationException + JSON mapping
│   │   └── util/                     # JwtUtil, PasswordUtil, JsonUtil
│   ├── lib/                          # Third-party jars (MariaDB driver, Gson, jBCrypt)
│   ├── schema.sql                    # Reference copy of the DB schema
│   ├── build.sh                      # Compiles the project (javac)
│   └── run.sh                        # Runs the compiled project (java)
├── frontend/
│   ├── index.html, login.html, register.html, products.html,
│   │   product-details.html, cart.html, checkout.html,
│   │   orders.html, profile.html, admin.html
│   ├── css/style.css
│   └── js/
│       ├── api.js       # fetch wrapper + auth/session storage
│       ├── main.js      # shared navbar/footer + route guards
│       └── <page>.js    # one file per page
└── README.md            # this file
```

---

## 3. Prerequisites

- **Java 17 or newer** (JDK, not just JRE — you need `javac`).
  Check with: `java -version` and `javac -version`.
- **MySQL 8.x** server, running and reachable on `localhost:3306`.
- A terminal. No IDE, Maven, or Node.js required.

---

## 4. Database Setup

1. Start MySQL if it isn't already running:
   ```bash
   sudo service mysql start
   # or, on systemd machines:
   sudo systemctl start mysql
   ```

2. Create the database and a dedicated application user (matches the
   credentials in `backend/src/com/babyshop/config/AppConfig.java`):
   ```bash
   sudo mysql -u root <<'EOF'
   CREATE DATABASE IF NOT EXISTS babyshop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'babyshop_user'@'localhost' IDENTIFIED BY 'babyshop_pass123';
   GRANT ALL PRIVILEGES ON babyshop_db.* TO 'babyshop_user'@'localhost';
   FLUSH PRIVILEGES;
   EOF
   ```

   > You do **not** need to run `schema.sql` by hand — the app calls
   > `CREATE TABLE IF NOT EXISTS` for all 7 tables automatically on startup
   > (see `Db.initSchema()`). `schema.sql` is provided for reference / in
   > case you want to inspect or run the DDL manually.

3. If your MySQL root password or the app's DB credentials are different
   from the defaults above, edit them in one place:
   `backend/src/com/babyshop/config/AppConfig.java` (`DB_URL`, `DB_USER`,
   `DB_PASSWORD`).

---

## 5. Build & Run

From the `backend/` directory:

```bash
cd babyshop/backend
./build.sh   # compiles everything into backend/bin/
./run.sh     # starts the server on http://localhost:8080
```

(If the scripts aren't executable: `chmod +x build.sh run.sh`.)

You should see:
```
>>> Database schema verified/created.
>>> Seeded default admin user: admin@babyshop.com / Admin@123
>>> Seeded categories and sample products
>>> Server running at http://localhost:8080
```

The very first run automatically seeds:
- **12 sample products** across **6 categories** (diapers, feeding, clothing, toys, bath, gear)
- **One admin account**: `admin@babyshop.com` / `Admin@123`

Subsequent runs skip seeding if data already exists.

### Using the app

Open your browser to:

**http://localhost:8080/index.html**

The same Java process serves both the REST API (under `/api/**`) and the
static frontend (`/`, `/products.html`, etc.) — there is nothing else to
start. Log in as a normal user by registering through the UI, or log in as
admin with the credentials above to reach the Admin dashboard link in the
navbar.

### Manual/raw commands (equivalent to build.sh / run.sh)

```bash
cd babyshop/backend
find src -name "*.java" > sources.txt
javac -cp "lib/*" -d bin @sources.txt
java -cp "bin:lib/*" com.babyshop.Main
```

---

## 6. Default / Test Accounts

| Role     | Email                 | Password   |
|----------|------------------------|------------|
| Admin    | admin@babyshop.com     | Admin@123  |
| Customer | *(register your own via the Sign Up page)* | — |

---

## 7. REST API Reference

All endpoints are under `http://localhost:8080/api`. Protected endpoints
require header `Authorization: Bearer <token>` (token returned by
register/login).

### Auth (public)
| Method | Path                | Body                                          |
|--------|----------------------|------------------------------------------------|
| POST   | `/auth/register`     | `{ name, email, phone, password }`             |
| POST   | `/auth/login`        | `{ email, password }`                          |

### Products & Categories (public)
| Method | Path                                  |
|--------|-----------------------------------------|
| GET    | `/products` (optional `?keyword=` or `?categoryId=`) |
| GET    | `/products/{id}`                        |
| GET    | `/categories`                           |
| GET    | `/categories/{id}`                      |

### Profile (authenticated)
| Method | Path         | Body                     |
|--------|--------------|--------------------------|
| GET    | `/users/me`  | —                        |
| PUT    | `/users/me`  | `{ name, phone }`        |

### Cart (authenticated)
| Method | Path                     | Body                          |
|--------|---------------------------|--------------------------------|
| GET    | `/cart`                   | —                               |
| POST   | `/cart/items`              | `{ productId, quantity }`      |
| PUT    | `/cart/items/{itemId}`     | `{ quantity }`                 |
| DELETE | `/cart/items/{itemId}`     | —                               |

### Orders (authenticated)
| Method | Path                | Body                                     |
|--------|----------------------|--------------------------------------------|
| POST   | `/orders/checkout`   | `{ shippingAddress, contactPhone }`         |
| GET    | `/orders`            | — (current user's order history)            |
| GET    | `/orders/{id}`        | — (own order, or any order if admin)         |

### Admin (authenticated + ROLE_ADMIN)
| Method | Path                          | Body                                                        |
|--------|--------------------------------|---------------------------------------------------------------|
| GET    | `/admin/products`              | —                                                               |
| POST   | `/admin/products`              | `{ name, description, price, stockQuantity, imageUrl, brand, categoryId }` |
| PUT    | `/admin/products/{id}`         | same as above                                                   |
| DELETE | `/admin/products/{id}`         | — (soft delete: marks inactive)                                 |
| POST   | `/admin/categories`            | `{ name, description }`                                         |
| PUT    | `/admin/categories/{id}`       | `{ name, description }`                                         |
| DELETE | `/admin/categories/{id}`       | — (fails with 409 if products still reference it)               |
| GET    | `/admin/users`                 | —                                                               |
| GET    | `/admin/orders`                | — (every customer's orders)                                     |
| PUT    | `/admin/orders/{id}/status`    | `{ status }` (`PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED`)  |

### Error shape
Every error response has this shape:
```json
{
  "timestamp": "2026-09-16T00:41:59.59",
  "status": 400,
  "message": "Validation failed",
  "path": "/api/auth/register",
  "fieldErrors": { "email": "Email must be a valid email address" }
}
```

---

## 8. Database Schema

7 tables with proper primary/foreign keys (see `backend/schema.sql` for
full DDL):

```
users            (id PK, name, email UNIQUE, phone, password, role, enabled, created_at)
categories       (id PK, name UNIQUE, description)
products         (id PK, name, description, price, stock_quantity, image_url, brand,
                  category_id FK -> categories.id, active, created_at, updated_at)
cart             (id PK, user_id FK -> users.id, UNIQUE)         -- one cart per user
cart_items       (id PK, cart_id FK -> cart.id, product_id FK -> products.id, quantity)
orders           (id PK, user_id FK -> users.id, total_amount, status,
                  shipping_address, contact_phone, created_at, updated_at)
order_items      (id PK, order_id FK -> orders.id, product_id FK -> products.id (nullable),
                  product_name, unit_price, quantity, subtotal)
```

Notable design choices:
- **`order_items` snapshots `product_name`/`unit_price`** at the moment of
  purchase, so editing or deleting a product later never changes historical
  orders. `product_id` is nullable with `ON DELETE SET NULL` for the same reason.
- **Products are soft-deleted** (`active = 0`) rather than hard-deleted by
  the admin "Delete" button, again to preserve order history integrity.
- **Checkout is a single JDBC transaction** (`OrderDao.checkout`): it uses
  `SELECT ... FOR UPDATE` to lock each product row, re-validates stock,
  decrements it, inserts the order + order_items, and empties the cart —
  all committed together or rolled back together if anything fails (e.g.
  someone else bought the last unit a second earlier).

---

## 9. Architecture & Data Flow

```
 Browser (HTML/CSS/JS)
      |
      | fetch() with JSON, JWT in Authorization header
      v
 com.sun.net.httpserver.HttpServer  (Main.java)
      |
      | Router matches (method, path) -> Handler   [like @GetMapping/@PostMapping]
      v
 Handler (controller layer)
      |
      | ctx.requireAuth() / ctx.requireAdmin() validates the JWT,
      | loads the User row, enforces role                [like Spring Security]
      | ctx.readBody(Class) parses the JSON request body   [like @RequestBody]
      v
 Service (business logic layer)
      |
      | validates input, applies business rules
      | (stock checks, duplicate email checks, price math, etc.)
      v
 DAO (data access layer)
      |
      | hand-written SQL via JDBC PreparedStatements
      v
 MySQL (babyshop_db)
```

**Request lifecycle example — placing an order:**
1. Frontend (`checkout.js`) calls `Api.checkout({shippingAddress, contactPhone})`,
   which does `fetch(".../api/orders/checkout", { method: "POST", headers: { Authorization: "Bearer <jwt>" }, body })`.
2. `Router` matches `POST /api/orders/checkout` to `OrderHandler.checkout`.
3. `OrderHandler` calls `ctx.requireAuth()` — this reads the `Authorization`
   header, validates the JWT's signature and expiry (`JwtUtil`), extracts the
   user's email from the token, and loads the full `User` row from MySQL via
   `UserDao`. If the token is missing/invalid/expired, a `401` JSON error is
   returned immediately and no business logic runs.
4. `OrderHandler` reads and validates the request body, then calls
   `OrderService.checkout(user, shippingAddress, contactPhone)`.
5. `OrderService` loads the user's cart (`CartService` -> `CartDao`), checks
   it isn't empty, then calls `OrderDao.checkout(...)`.
6. `OrderDao.checkout` opens **one JDBC connection with `setAutoCommit(false)`**
   and, for every cart item: locks the product row (`FOR UPDATE`), re-checks
   stock, decrements it, accumulates the order total. It then inserts the
   `orders` row, batch-inserts `order_items`, deletes the `cart_items` rows,
   and calls `conn.commit()`. If any step throws (e.g. insufficient stock),
   the `catch` block calls `conn.rollback()` — so a failed checkout leaves
   the database exactly as it was before, with no partial state.
7. The resulting `Order` is converted to an `OrderResponse` DTO and sent
   back as JSON.
8. The frontend redirects to `orders.html?placed=<id>`, which calls
   `GET /api/orders` and renders the order history, including the just-placed
   order highlighted with a success banner.

**Authentication/authorization in one sentence:** every protected request
carries a JWT in its `Authorization` header; `RequestContext.requireAuth()`
verifies the token's HMAC-SHA256 signature and expiry, resolves it to a real
`User` row in MySQL, and `requireAdmin()` additionally checks
`user.getRole() == ROLE_ADMIN` — throwing a `401` or `403` (caught centrally
by the `Router` and turned into the standard JSON error shape) if either
check fails.

---

## 10. What Was Tested

All of the following were exercised against the **real, running MySQL
database** (no mocked responses) during development:

- Register a new customer -> JWT issued -> row inserted in `users`, cart auto-created
- Duplicate email registration -> `409 Conflict`
- Invalid email / short password -> `400` with field-level `fieldErrors`
- Login with correct / incorrect password -> `200` / `401`
- Browse all products, filter by category, keyword search (name/brand/category)
- View single product details
- Add to cart, add more of the same item (quantities merge), update quantity, remove item
- Adding more than available stock -> `400` with a clear message
- Checkout: order created, stock decremented in MySQL, cart emptied, order appears in history
- View order history and a single order's details
- Update profile (name/phone)
- Admin: create/update/soft-delete product; create/update category;
  delete category blocked by FK constraint (`409`) while products reference it
- Admin: view all users, view all orders, update an order's status
- Non-admin hitting `/api/admin/**` -> `403 Forbidden`; no token -> `401 Unauthorized`
- All 10 frontend pages and all CSS/JS assets served correctly (`200`) from the same server
- Fresh database (`DROP DATABASE` + recreate) auto-seeds correctly on next startup

---

## 11. Porting to Spring Boot (optional)

If you want the "canonical" Spring Boot + Maven version of this same
design, the mapping is direct:

| This project             | Spring Boot equivalent                          |
|----------------------------|----------------------------------------------------|
| `model/*.java`             | `@Entity` classes (add JPA annotations)             |
| `dao/*.java`                | `interface XRepository extends JpaRepository<...>` |
| `service/*.java`            | `@Service` classes (almost unchanged)               |
| `handler/*.java`            | `@RestController` classes (unchanged method bodies) |
| `router/RequestContext`    | Spring's `@RequestBody`/`@PathVariable`/`Authentication` |
| `util/JwtUtil`              | Same logic, or swap in `jjwt` library                |
| `exception/*`               | `@RestControllerAdvice` + `@ExceptionHandler`        |
| `Db.initSchema()`           | `spring.jpa.hibernate.ddl-auto=update`               |
| `Main.java` wiring          | Spring's dependency injection (`@Autowired`)          |

Every SQL query, validation rule, and business rule in this project can be
copied over essentially unchanged — only the "plumbing" (how objects are
constructed and how HTTP requests are routed) differs.
