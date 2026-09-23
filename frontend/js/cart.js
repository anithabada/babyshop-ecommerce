async function loadCart() {
    const content = document.getElementById("cart-content");
    try {
        const cart = await Api.getCart();

        if (!cart.items || cart.items.length === 0) {
            content.innerHTML = `
              <div class="empty-state">
                <p>Your cart is empty.</p>
                <a href="products.html" class="btn btn-primary">Browse Products</a>
              </div>`;
            return;
        }

        const itemsHtml = cart.items.map(item => `
          <div class="cart-item" data-item-id="${item.id}">
            <img src="${escapeHtml(item.imageUrl || 'https://via.placeholder.com/80?text=Baby')}" alt="${escapeHtml(item.productName)}">
            <div class="details">
              <div style="font-weight:700;">${escapeHtml(item.productName)}</div>
              <div style="color:var(--muted); font-size:0.85rem;">${formatCurrency(item.unitPrice)} each &middot; ${item.availableStock} in stock</div>
              <div class="qty-control" style="margin-top:8px;">
                <button class="qty-btn item-minus">−</button>
                <input type="number" class="qty-input item-qty" value="${item.quantity}" min="1" max="${item.availableStock}">
                <button class="qty-btn item-plus">+</button>
              </div>
            </div>
            <div style="text-align:right;">
              <div style="font-weight:800; color:var(--accent-dark); margin-bottom:8px;">${formatCurrency(item.subtotal)}</div>
              <button class="icon-btn item-remove">Remove</button>
            </div>
          </div>
        `).join("");

        content.innerHTML = `
          <div class="cart-layout">
            <div class="card">
              <div id="cart-alert"></div>
              ${itemsHtml}
            </div>
            <div class="cart-summary">
              <div class="summary-row"><span>Items</span><span>${cart.totalItems}</span></div>
              <div class="summary-row total"><span>Total</span><span>${formatCurrency(cart.totalAmount)}</span></div>
              <a href="checkout.html" class="btn btn-accent btn-block" style="margin-top:14px;">Proceed to Checkout</a>
            </div>
          </div>`;

        attachCartHandlers();
    } catch (e) {
        content.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

function attachCartHandlers() {
    document.querySelectorAll(".cart-item").forEach(row => {
        const itemId = row.dataset.itemId;
        const qtyInput = row.querySelector(".item-qty");
        const max = Number(qtyInput.getAttribute("max"));

        row.querySelector(".item-minus").addEventListener("click", () => {
            const newQty = Math.max(1, Number(qtyInput.value) - 1);
            updateQty(itemId, newQty);
        });
        row.querySelector(".item-plus").addEventListener("click", () => {
            const newQty = Math.min(max, Number(qtyInput.value) + 1);
            updateQty(itemId, newQty);
        });
        qtyInput.addEventListener("change", () => {
            let newQty = Number(qtyInput.value);
            if (newQty < 1) newQty = 1;
            if (newQty > max) newQty = max;
            updateQty(itemId, newQty);
        });
        row.querySelector(".item-remove").addEventListener("click", () => removeItem(itemId));
    });
}

async function updateQty(itemId, quantity) {
    const alertBox = document.getElementById("cart-alert");
    try {
        await Api.updateCartItem(itemId, quantity);
        await loadCart();
        refreshCartBadge();
    } catch (e) {
        alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

async function removeItem(itemId) {
    const alertBox = document.getElementById("cart-alert");
    try {
        await Api.removeCartItem(itemId);
        await loadCart();
        refreshCartBadge();
    } catch (e) {
        alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    if (!requireAuth()) return;
    loadCart();
});
