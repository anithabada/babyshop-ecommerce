/* =========================================================
   Baby Shop - API helper
   Central place for talking to the Spring Boot backend and
   for managing the JWT token / logged-in user in localStorage.
   ========================================================= */

// Change this if your backend runs on a different host/port.
const API_BASE = "http://localhost:8080/api";

const Auth = {
    getToken() { return localStorage.getItem("babyshop_token"); },
    getUser() {
        const raw = localStorage.getItem("babyshop_user");
        return raw ? JSON.parse(raw) : null;
    },
    setSession(authResponse) {
        localStorage.setItem("babyshop_token", authResponse.token);
        localStorage.setItem("babyshop_user", JSON.stringify({
            id: authResponse.userId,
            name: authResponse.name,
            email: authResponse.email,
            role: authResponse.role
        }));
    },
    clearSession() {
        localStorage.removeItem("babyshop_token");
        localStorage.removeItem("babyshop_user");
    },
    isLoggedIn() { return !!this.getToken(); },
    isAdmin() {
        const u = this.getUser();
        return !!u && u.role === "ROLE_ADMIN";
    }
};

/**
 * Core request helper. Automatically attaches the JWT (if present) and
 * parses JSON. Throws an Error with a readable message on failure so
 * callers can show it to the user.
 */
async function apiRequest(path, { method = "GET", body = null, auth = false } = {}) {
    const headers = { "Content-Type": "application/json" };

    if (auth) {
        const token = Auth.getToken();
        if (!token) {
            throw new Error("You must be logged in to do this.");
        }
        headers["Authorization"] = "Bearer " + token;
    }

    let response;
    try {
        response = await fetch(API_BASE + path, {
            method,
            headers,
            body: body ? JSON.stringify(body) : undefined
        });
    } catch (networkErr) {
        throw new Error("Could not reach the server. Is the backend running on " + API_BASE + " ?");
    }

    // Session expired / invalid token
    if (response.status === 401 && auth) {
        Auth.clearSession();
    }

    let data = null;
    const text = await response.text();
    if (text) {
        try { data = JSON.parse(text); } catch (e) { data = null; }
    }

    if (!response.ok) {
        const message = (data && (data.message || data.error)) || ("Request failed with status " + response.status);
        const err = new Error(message);
        err.fieldErrors = data ? data.fieldErrors : null;
        err.status = response.status;
        throw err;
    }

    return data;
}

const Api = {
    // ----- Auth -----
    register: (payload) => apiRequest("/auth/register", { method: "POST", body: payload }),
    login: (payload) => apiRequest("/auth/login", { method: "POST", body: payload }),

    // ----- Products / Categories (public) -----
    getProducts: (params = {}) => {
        const qs = new URLSearchParams(params).toString();
        return apiRequest("/products" + (qs ? "?" + qs : ""));
    },
    getProduct: (id) => apiRequest("/products/" + id),
    getCategories: () => apiRequest("/categories"),

    // ----- Profile -----
    getMyProfile: () => apiRequest("/users/me", { auth: true }),
    updateMyProfile: (payload) => apiRequest("/users/me", { method: "PUT", body: payload, auth: true }),

    // ----- Cart -----
    getCart: () => apiRequest("/cart", { auth: true }),
    addToCart: (payload) => apiRequest("/cart/items", { method: "POST", body: payload, auth: true }),
    updateCartItem: (itemId, quantity) => apiRequest("/cart/items/" + itemId, { method: "PUT", body: { quantity }, auth: true }),
    removeCartItem: (itemId) => apiRequest("/cart/items/" + itemId, { method: "DELETE", auth: true }),

    // ----- Orders -----
    checkout: (payload) => apiRequest("/orders/checkout", { method: "POST", body: payload, auth: true }),
    getMyOrders: () => apiRequest("/orders", { auth: true }),
    getOrder: (id) => apiRequest("/orders/" + id, { auth: true }),

    // ----- Admin -----
    admin: {
        getProducts: () => apiRequest("/admin/products", { auth: true }),
        createProduct: (payload) => apiRequest("/admin/products", { method: "POST", body: payload, auth: true }),
        updateProduct: (id, payload) => apiRequest("/admin/products/" + id, { method: "PUT", body: payload, auth: true }),
        deleteProduct: (id) => apiRequest("/admin/products/" + id, { method: "DELETE", auth: true }),

        createCategory: (payload) => apiRequest("/admin/categories", { method: "POST", body: payload, auth: true }),
        updateCategory: (id, payload) => apiRequest("/admin/categories/" + id, { method: "PUT", body: payload, auth: true }),
        deleteCategory: (id) => apiRequest("/admin/categories/" + id, { method: "DELETE", auth: true }),

        getUsers: () => apiRequest("/admin/users", { auth: true }),

        getOrders: () => apiRequest("/admin/orders", { auth: true }),
        updateOrderStatus: (id, status) => apiRequest("/admin/orders/" + id + "/status", { method: "PUT", body: { status }, auth: true })
    }
};

function formatCurrency(amount) {
    const n = Number(amount);
    return "₹" + n.toLocaleString("en-IN", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}
