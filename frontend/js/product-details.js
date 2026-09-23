async function loadProduct() {
    const params = new URLSearchParams(window.location.search);
    const id = params.get("id");
    const content = document.getElementById("pd-content");

    if (!id) {
        content.innerHTML = `<p class="empty-state">No product specified.</p>`;
        return;
    }

    try {
        const p = await Api.getProduct(id);
        const outOfStock = p.stockQuantity <= 0;

        content.innerHTML = `
        <div class="pd-layout">
          <img src="${escapeHtml(p.imageUrl || 'https://via.placeholder.com/500x400?text=Baby+Product')}" alt="${escapeHtml(p.name)}">
          <div>
            <span class="brand">${escapeHtml(p.brand)}</span>
            <h1>${escapeHtml(p.name)}</h1>
            <div style="color:var(--muted); margin-bottom: 6px;">Category: ${escapeHtml(p.categoryName)}</div>
            <div class="pd-price">${formatCurrency(p.price)}</div>
            <p>${escapeHtml(p.description || "No description available.")}</p>
            <p class="stock">${outOfStock ? "Out of stock" : p.stockQuantity + " units in stock"}</p>

            <div id="pd-alert"></div>

            <div style="display:flex; align-items:center; gap:12px; margin-top:18px;">
              <div class="qty-control">
                <button class="qty-btn" id="qty-minus">−</button>
                <input type="number" id="qty-input" class="qty-input" value="1" min="1" max="${p.stockQuantity}">
                <button class="qty-btn" id="qty-plus">+</button>
              </div>
              <button class="btn btn-accent" id="add-cart-btn" ${outOfStock ? "disabled" : ""}>
                ${outOfStock ? "Out of Stock" : "Add to Cart"}
              </button>
            </div>
          </div>
        </div>`;

        document.title = p.name + " - BabyShop";

        const qtyInput = document.getElementById("qty-input");
        document.getElementById("qty-minus").addEventListener("click", () => {
            qtyInput.value = Math.max(1, Number(qtyInput.value) - 1);
        });
        document.getElementById("qty-plus").addEventListener("click", () => {
            qtyInput.value = Math.min(p.stockQuantity, Number(qtyInput.value) + 1);
        });

        document.getElementById("add-cart-btn").addEventListener("click", async () => {
            const alertBox = document.getElementById("pd-alert");
            alertBox.innerHTML = "";

            if (!Auth.isLoggedIn()) {
                window.location.href = "login.html?redirect=product-details.html?id=" + p.id;
                return;
            }

            const qty = Number(qtyInput.value) || 1;
            try {
                await Api.addToCart({ productId: p.id, quantity: qty });
                alertBox.innerHTML = `<div class="alert alert-success">Added to cart!</div>`;
                refreshCartBadge();
            } catch (e) {
                alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
            }
        });

    } catch (e) {
        content.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

document.addEventListener("DOMContentLoaded", loadProduct);
