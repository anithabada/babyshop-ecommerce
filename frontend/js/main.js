/* =========================================================
   Baby Shop - Shared layout (navbar + footer)
   Injects the navbar into #navbar-root and footer into #footer-root
   on every page, and keeps the cart-count badge up to date.
   ========================================================= */

function renderNavbar() {
    const root = document.getElementById("navbar-root");
    if (!root) return;

    const user = Auth.getUser();
    const loggedIn = Auth.isLoggedIn();
    const isAdmin = Auth.isAdmin();

    root.innerHTML = `
    <nav class="navbar">
      <div class="container">
        <a href="index.html" class="brand">🍼 BabyShop</a>
        <div class="nav-links">
          <a href="index.html">Home</a>
          <a href="products.html">Shop</a>
          ${loggedIn ? `<a href="cart.html">Cart <span class="cart-badge" id="cart-count">0</span></a>` : ""}
          ${loggedIn ? `<a href="orders.html">My Orders</a>` : ""}
          ${loggedIn ? `<a href="profile.html">Profile</a>` : ""}
          ${isAdmin ? `<a href="admin.html">Admin</a>` : ""}
          ${loggedIn
            ? `<span style="color:var(--muted); font-size:0.85rem;">Hi, ${escapeHtml(user.name.split(" ")[0])}</span><button id="logout-btn">Logout</button>`
            : `<a href="login.html">Login</a><a href="register.html" class="btn btn-primary btn-sm">Sign Up</a>`}
        </div>
      </div>
    </nav>`;

    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            Auth.clearSession();
            window.location.href = "index.html";
        });
    }

    if (loggedIn) refreshCartBadge();
}

async function refreshCartBadge() {
    const badge = document.getElementById("cart-count");
    if (!badge) return;
    try {
        const cart = await Api.getCart();
        badge.textContent = cart.totalItems || 0;
    } catch (e) {
        // silently ignore (e.g. token expired) - user will see it on protected pages
    }
}

function renderFooter() {
    const root = document.getElementById("footer-root");
    if (!root) return;
    root.innerHTML = `
    <footer class="footer">
      <div class="container">
        &copy; ${new Date().getFullYear()} BabyShop &mdash; Everything your little one needs, delivered with care.
      </div>
    </footer>`;
}

/** Redirects to login.html if the user isn't authenticated. Call at top of protected pages. */
function requireAuth() {
    if (!Auth.isLoggedIn()) {
        window.location.href = "login.html?redirect=" + encodeURIComponent(window.location.pathname.split("/").pop());
        return false;
    }
    return true;
}

/** Redirects home if the user isn't an admin. Call at top of admin.html. */
function requireAdmin() {
    if (!Auth.isLoggedIn() || !Auth.isAdmin()) {
        window.location.href = "index.html";
        return false;
    }
    return true;
}

document.addEventListener("DOMContentLoaded", () => {
    renderNavbar();
    renderFooter();
});
