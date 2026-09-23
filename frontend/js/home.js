async function loadCategories() {
    const grid = document.getElementById("category-grid");
    try {
        const categories = await Api.getCategories();
        if (categories.length === 0) {
            grid.innerHTML = `<p class="empty-state">No categories yet.</p>`;
            return;
        }
        grid.innerHTML = categories.map(c => `
            <a class="category-chip" href="products.html?categoryId=${c.id}">${escapeHtml(c.name)}</a>
        `).join("");
    } catch (e) {
        grid.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

function productCardHtml(p) {
    const outOfStock = p.stockQuantity <= 0;
    return `
    <div class="product-card">
      <a href="product-details.html?id=${p.id}">
        <img src="${escapeHtml(p.imageUrl || 'https://via.placeholder.com/300x200?text=Baby+Product')}" alt="${escapeHtml(p.name)}">
      </a>
      <div class="info">
        <span class="brand">${escapeHtml(p.brand)}</span>
        <a href="product-details.html?id=${p.id}"><div class="name">${escapeHtml(p.name)}</div></a>
        <div class="price">${formatCurrency(p.price)}</div>
        <div class="stock">${outOfStock ? "Out of stock" : p.stockQuantity + " in stock"}</div>
        <div class="actions">
          <a href="product-details.html?id=${p.id}" class="btn btn-outline btn-sm btn-block">View</a>
        </div>
      </div>
    </div>`;
}

async function loadFeatured() {
    const grid = document.getElementById("featured-grid");
    try {
        const products = await Api.getProducts();
        if (products.length === 0) {
            grid.innerHTML = `<p class="empty-state">No products available yet.</p>`;
            return;
        }
        grid.innerHTML = products.slice(0, 8).map(productCardHtml).join("");
    } catch (e) {
        grid.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadCategories();
    loadFeatured();
});
