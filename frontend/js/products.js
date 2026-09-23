let currentCategoryId = null;

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

async function loadCategoryFilter() {
    const wrap = document.getElementById("category-filter");
    try {
        const categories = await Api.getCategories();
        wrap.innerHTML = `<div class="category-chip ${currentCategoryId === null ? 'active' : ''}" data-id="">All</div>` +
            categories.map(c => `<div class="category-chip ${String(currentCategoryId) === String(c.id) ? 'active' : ''}" data-id="${c.id}">${escapeHtml(c.name)}</div>`).join("");

        wrap.querySelectorAll(".category-chip").forEach(chip => {
            chip.addEventListener("click", () => {
                currentCategoryId = chip.dataset.id || null;
                document.getElementById("search-input").value = "";
                loadProducts();
                loadCategoryFilter();
            });
        });
    } catch (e) {
        wrap.innerHTML = "";
    }
}

async function loadProducts() {
    const grid = document.getElementById("product-grid");
    grid.innerHTML = `<div class="loading">Loading products...</div>`;

    const keyword = document.getElementById("search-input").value.trim();
    const params = {};
    if (keyword) params.keyword = keyword;
    else if (currentCategoryId) params.categoryId = currentCategoryId;

    try {
        const products = await Api.getProducts(params);
        if (products.length === 0) {
            grid.innerHTML = `<p class="empty-state">No products found. Try a different search or category.</p>`;
            return;
        }
        grid.innerHTML = products.map(productCardHtml).join("");
    } catch (e) {
        grid.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const params = new URLSearchParams(window.location.search);
    currentCategoryId = params.get("categoryId");

    loadCategoryFilter();
    loadProducts();

    document.getElementById("search-btn").addEventListener("click", () => {
        currentCategoryId = null;
        loadProducts();
        loadCategoryFilter();
    });
    document.getElementById("search-input").addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            currentCategoryId = null;
            loadProducts();
            loadCategoryFilter();
        }
    });
});
