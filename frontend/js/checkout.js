async function loadCheckout() {
    const content = document.getElementById("checkout-content");
    try {
        const [cart, profile] = await Promise.all([Api.getCart(), Api.getMyProfile()]);

        if (!cart.items || cart.items.length === 0) {
            content.innerHTML = `
              <div class="empty-state">
                <p>Your cart is empty. Add some products before checking out.</p>
                <a href="products.html" class="btn btn-primary">Browse Products</a>
              </div>`;
            return;
        }

        const itemsHtml = cart.items.map(item => `
          <div class="summary-row"><span>${escapeHtml(item.productName)} &times; ${item.quantity}</span><span>${formatCurrency(item.subtotal)}</span></div>
        `).join("");

        content.innerHTML = `
          <div class="cart-layout">
            <div class="card">
              <h3 style="margin-top:0;">Shipping Details</h3>
              <div id="checkout-alert"></div>
              <form id="checkout-form">
                <div class="field">
                  <label for="address">Shipping Address</label>
                  <textarea id="address" rows="3" required placeholder="House no., street, city, state, PIN code"></textarea>
                </div>
                <div class="field">
                  <label for="phone">Contact Phone</label>
                  <input type="text" id="phone" value="${escapeHtml(profile.phone)}" required>
                </div>
                <button type="submit" class="btn btn-accent btn-block" id="place-order-btn">Place Order</button>
              </form>
            </div>
            <div class="cart-summary">
              <h3 style="margin-top:0;">Order Summary</h3>
              ${itemsHtml}
              <div class="summary-row total"><span>Total</span><span>${formatCurrency(cart.totalAmount)}</span></div>
            </div>
          </div>`;

        document.getElementById("checkout-form").addEventListener("submit", async (e) => {
            e.preventDefault();
            const alertBox = document.getElementById("checkout-alert");
            const btn = document.getElementById("place-order-btn");
            alertBox.innerHTML = "";

            const payload = {
                shippingAddress: document.getElementById("address").value.trim(),
                contactPhone: document.getElementById("phone").value.trim()
            };

            btn.disabled = true;
            btn.textContent = "Placing order...";

            try {
                const order = await Api.checkout(payload);
                window.location.href = "orders.html?placed=" + order.id;
            } catch (err) {
                alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
                btn.disabled = false;
                btn.textContent = "Place Order";
            }
        });

    } catch (e) {
        content.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    if (!requireAuth()) return;
    loadCheckout();
});
