function orderCardHtml(order, justPlaced) {
    const itemsHtml = order.items.map(i => `
      <div class="summary-row"><span>${escapeHtml(i.productName)} &times; ${i.quantity}</span><span>${formatCurrency(i.subtotal)}</span></div>
    `).join("");

    return `
    <div class="card" style="margin-bottom:18px;">
      ${justPlaced ? `<div class="alert alert-success">Order placed successfully!</div>` : ""}
      <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;">
        <div>
          <div style="font-weight:800;">Order #${order.id}</div>
          <div style="color:var(--muted); font-size:0.85rem;">${new Date(order.createdAt).toLocaleString()}</div>
        </div>
        <span class="badge badge-${order.status}">${order.status}</span>
      </div>
      <hr style="border:none; border-top:1px solid var(--border); margin:14px 0;">
      ${itemsHtml}
      <div class="summary-row total"><span>Total</span><span>${formatCurrency(order.totalAmount)}</span></div>
      <div style="margin-top:10px; font-size:0.85rem; color:var(--muted);">
        Shipping to: ${escapeHtml(order.shippingAddress)} &middot; ${escapeHtml(order.contactPhone)}
      </div>
    </div>`;
}

async function loadOrders() {
    const content = document.getElementById("orders-content");
    const params = new URLSearchParams(window.location.search);
    const justPlacedId = params.get("placed");

    try {
        const orders = await Api.getMyOrders();

        if (orders.length === 0) {
            content.innerHTML = `
              <div class="empty-state">
                <p>You haven't placed any orders yet.</p>
                <a href="products.html" class="btn btn-primary">Start Shopping</a>
              </div>`;
            return;
        }

        content.innerHTML = orders.map(o => orderCardHtml(o, String(o.id) === justPlacedId)).join("");
    } catch (e) {
        content.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    if (!requireAuth()) return;
    loadOrders();
});
