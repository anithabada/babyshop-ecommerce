/* =========================================================
   Baby Shop - Admin Dashboard
   ========================================================= */

let categoriesCache = [];

function showAdminAlert(message, type = "error") {
    const box = document.getElementById("admin-alert");
    box.innerHTML = `<div class="alert alert-${type}">${escapeHtml(message)}</div>`;
    setTimeout(() => { box.innerHTML = ""; }, 4000);
}

function openModal(html) {
    const root = document.getElementById("modal-root");
    root.innerHTML = `<div class="modal-overlay" id="modal-overlay"><div class="modal-box">${html}</div></div>`;
    document.getElementById("modal-overlay").addEventListener("click", (e) => {
        if (e.target.id === "modal-overlay") closeModal();
    });
}
function closeModal() {
    document.getElementById("modal-root").innerHTML = "";
}

// ---------------- PRODUCTS ----------------

async function renderProductsTab() {
    const panel = document.getElementById("admin-panel");
    panel.innerHTML = `<div class="loading">Loading products...</div>`;
    try {
        const [products, categories] = await Promise.all([Api.admin.getProducts(), Api.getCategories()]);
        categoriesCache = categories;

        panel.innerHTML = `
          <div class="card">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
              <h3 style="margin:0;">Products (${products.length})</h3>
              <button class="btn btn-primary btn-sm" id="add-product-btn">+ Add Product</button>
            </div>
            <table>
              <thead><tr><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th></th></tr></thead>
              <tbody>
                ${products.map(p => `
                  <tr>
                    <td>${escapeHtml(p.name)}</td>
                    <td>${escapeHtml(p.categoryName)}</td>
                    <td>${formatCurrency(p.price)}</td>
                    <td>${p.stockQuantity}</td>
                    <td>${p.active ? '<span class="badge badge-DELIVERED">Active</span>' : '<span class="badge badge-CANCELLED">Inactive</span>'}</td>
                    <td style="white-space:nowrap;">
                      <button class="btn btn-outline btn-sm edit-product-btn" data-id="${p.id}">Edit</button>
                      <button class="btn btn-danger btn-sm delete-product-btn" data-id="${p.id}" data-name="${escapeHtml(p.name)}">Delete</button>
                    </td>
                  </tr>
                `).join("")}
              </tbody>
            </table>
          </div>`;

        document.getElementById("add-product-btn").addEventListener("click", () => openProductModal(null, products));
        panel.querySelectorAll(".edit-product-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                const p = products.find(x => String(x.id) === btn.dataset.id);
                openProductModal(p);
            });
        });
        panel.querySelectorAll(".delete-product-btn").forEach(btn => {
            btn.addEventListener("click", () => deleteProduct(btn.dataset.id, btn.dataset.name));
        });
    } catch (e) {
        panel.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

function openProductModal(product) {
    const isEdit = !!product;
    const categoryOptions = categoriesCache.map(c =>
        `<option value="${c.id}" ${product && product.categoryId === c.id ? "selected" : ""}>${escapeHtml(c.name)}</option>`
    ).join("");

    openModal(`
      <h3>${isEdit ? "Edit Product" : "Add Product"}</h3>
      <div id="modal-alert"></div>
      <form id="product-form">
        <div class="field"><label>Name</label><input type="text" id="pf-name" value="${escapeHtml(product?.name || "")}" required></div>
        <div class="field"><label>Description</label><textarea id="pf-description" rows="3">${escapeHtml(product?.description || "")}</textarea></div>
        <div class="field"><label>Brand</label><input type="text" id="pf-brand" value="${escapeHtml(product?.brand || "")}" required></div>
        <div class="field"><label>Category</label><select id="pf-category" required>${categoryOptions}</select></div>
        <div class="field"><label>Price (₹)</label><input type="number" id="pf-price" min="0.01" step="0.01" value="${product?.price ?? ""}" required></div>
        <div class="field"><label>Stock Quantity</label><input type="number" id="pf-stock" min="0" step="1" value="${product?.stockQuantity ?? ""}" required></div>
        <div class="field"><label>Image URL</label><input type="text" id="pf-image" value="${escapeHtml(product?.imageUrl || "")}"></div>
        <div style="display:flex; gap:10px; margin-top:10px;">
          <button type="button" class="btn btn-outline btn-block" id="modal-cancel-btn">Cancel</button>
          <button type="submit" class="btn btn-primary btn-block" id="modal-submit-btn">${isEdit ? "Save Changes" : "Add Product"}</button>
        </div>
      </form>
    `);

    document.getElementById("modal-cancel-btn").addEventListener("click", closeModal);
    document.getElementById("product-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const modalAlert = document.getElementById("modal-alert");
        const submitBtn = document.getElementById("modal-submit-btn");

        const payload = {
            name: document.getElementById("pf-name").value.trim(),
            description: document.getElementById("pf-description").value.trim(),
            brand: document.getElementById("pf-brand").value.trim(),
            categoryId: Number(document.getElementById("pf-category").value),
            price: Number(document.getElementById("pf-price").value),
            stockQuantity: Number(document.getElementById("pf-stock").value),
            imageUrl: document.getElementById("pf-image").value.trim()
        };

        submitBtn.disabled = true;
        try {
            if (isEdit) {
                await Api.admin.updateProduct(product.id, payload);
            } else {
                await Api.admin.createProduct(payload);
            }
            closeModal();
            showAdminAlert(isEdit ? "Product updated." : "Product added.", "success");
            renderProductsTab();
        } catch (err) {
            modalAlert.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
            submitBtn.disabled = false;
        }
    });
}

async function deleteProduct(id, name) {
    if (!confirm(`Delete "${name}"? This will remove it from the storefront.`)) return;
    try {
        await Api.admin.deleteProduct(id);
        showAdminAlert("Product deleted.", "success");
        renderProductsTab();
    } catch (e) {
        showAdminAlert(e.message);
    }
}

// ---------------- CATEGORIES ----------------

async function renderCategoriesTab() {
    const panel = document.getElementById("admin-panel");
    panel.innerHTML = `<div class="loading">Loading categories...</div>`;
    try {
        const categories = await Api.getCategories();
        categoriesCache = categories;

        panel.innerHTML = `
          <div class="card">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
              <h3 style="margin:0;">Categories (${categories.length})</h3>
              <button class="btn btn-primary btn-sm" id="add-category-btn">+ Add Category</button>
            </div>
            <table>
              <thead><tr><th>Name</th><th>Description</th><th></th></tr></thead>
              <tbody>
                ${categories.map(c => `
                  <tr>
                    <td>${escapeHtml(c.name)}</td>
                    <td>${escapeHtml(c.description || "-")}</td>
                    <td style="white-space:nowrap;">
                      <button class="btn btn-outline btn-sm edit-cat-btn" data-id="${c.id}">Edit</button>
                      <button class="btn btn-danger btn-sm delete-cat-btn" data-id="${c.id}" data-name="${escapeHtml(c.name)}">Delete</button>
                    </td>
                  </tr>
                `).join("")}
              </tbody>
            </table>
          </div>`;

        document.getElementById("add-category-btn").addEventListener("click", () => openCategoryModal(null));
        panel.querySelectorAll(".edit-cat-btn").forEach(btn => {
            btn.addEventListener("click", () => {
                const c = categories.find(x => String(x.id) === btn.dataset.id);
                openCategoryModal(c);
            });
        });
        panel.querySelectorAll(".delete-cat-btn").forEach(btn => {
            btn.addEventListener("click", () => deleteCategory(btn.dataset.id, btn.dataset.name));
        });
    } catch (e) {
        panel.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

function openCategoryModal(category) {
    const isEdit = !!category;
    openModal(`
      <h3>${isEdit ? "Edit Category" : "Add Category"}</h3>
      <div id="modal-alert"></div>
      <form id="category-form">
        <div class="field"><label>Name</label><input type="text" id="cf-name" value="${escapeHtml(category?.name || "")}" required></div>
        <div class="field"><label>Description</label><textarea id="cf-description" rows="2">${escapeHtml(category?.description || "")}</textarea></div>
        <div style="display:flex; gap:10px; margin-top:10px;">
          <button type="button" class="btn btn-outline btn-block" id="modal-cancel-btn">Cancel</button>
          <button type="submit" class="btn btn-primary btn-block" id="modal-submit-btn">${isEdit ? "Save Changes" : "Add Category"}</button>
        </div>
      </form>
    `);

    document.getElementById("modal-cancel-btn").addEventListener("click", closeModal);
    document.getElementById("category-form").addEventListener("submit", async (e) => {
        e.preventDefault();
        const modalAlert = document.getElementById("modal-alert");
        const submitBtn = document.getElementById("modal-submit-btn");

        const payload = {
            name: document.getElementById("cf-name").value.trim(),
            description: document.getElementById("cf-description").value.trim()
        };

        submitBtn.disabled = true;
        try {
            if (isEdit) {
                await Api.admin.updateCategory(category.id, payload);
            } else {
                await Api.admin.createCategory(payload);
            }
            closeModal();
            showAdminAlert(isEdit ? "Category updated." : "Category added.", "success");
            renderCategoriesTab();
        } catch (err) {
            modalAlert.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
            submitBtn.disabled = false;
        }
    });
}

async function deleteCategory(id, name) {
    if (!confirm(`Delete category "${name}"? This will fail if products still use it.`)) return;
    try {
        await Api.admin.deleteCategory(id);
        showAdminAlert("Category deleted.", "success");
        renderCategoriesTab();
    } catch (e) {
        showAdminAlert(e.message);
    }
}

// ---------------- ORDERS ----------------

const ORDER_STATUSES = ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED"];

async function renderOrdersTab() {
    const panel = document.getElementById("admin-panel");
    panel.innerHTML = `<div class="loading">Loading orders...</div>`;
    try {
        const orders = await Api.admin.getOrders();

        panel.innerHTML = `
          <div class="card">
            <h3 style="margin-top:0;">All Orders (${orders.length})</h3>
            <table>
              <thead><tr><th>Order</th><th>Customer</th><th>Date</th><th>Total</th><th>Status</th></tr></thead>
              <tbody>
                ${orders.map(o => `
                  <tr>
                    <td>#${o.id}</td>
                    <td>${escapeHtml(o.userName)}</td>
                    <td>${new Date(o.createdAt).toLocaleDateString()}</td>
                    <td>${formatCurrency(o.totalAmount)}</td>
                    <td>
                      <select class="status-select" data-id="${o.id}" style="padding:6px 10px; border-radius:8px; border:1.5px solid var(--border);">
                        ${ORDER_STATUSES.map(s => `<option value="${s}" ${s === o.status ? "selected" : ""}>${s}</option>`).join("")}
                      </select>
                    </td>
                  </tr>
                `).join("")}
              </tbody>
            </table>
          </div>`;

        panel.querySelectorAll(".status-select").forEach(select => {
            select.addEventListener("change", async () => {
                try {
                    await Api.admin.updateOrderStatus(select.dataset.id, select.value);
                    showAdminAlert(`Order #${select.dataset.id} updated to ${select.value}.`, "success");
                } catch (e) {
                    showAdminAlert(e.message);
                    renderOrdersTab();
                }
            });
        });
    } catch (e) {
        panel.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

// ---------------- USERS ----------------

async function renderUsersTab() {
    const panel = document.getElementById("admin-panel");
    panel.innerHTML = `<div class="loading">Loading users...</div>`;
    try {
        const users = await Api.admin.getUsers();

        panel.innerHTML = `
          <div class="card">
            <h3 style="margin-top:0;">Registered Users (${users.length})</h3>
            <table>
              <thead><tr><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th>Joined</th></tr></thead>
              <tbody>
                ${users.map(u => `
                  <tr>
                    <td>${escapeHtml(u.name)}</td>
                    <td>${escapeHtml(u.email)}</td>
                    <td>${escapeHtml(u.phone)}</td>
                    <td>${u.role === "ROLE_ADMIN" ? '<span class="badge badge-SHIPPED">Admin</span>' : '<span class="badge badge-CONFIRMED">Customer</span>'}</td>
                    <td>${new Date(u.createdAt).toLocaleDateString()}</td>
                  </tr>
                `).join("")}
              </tbody>
            </table>
          </div>`;
    } catch (e) {
        panel.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

// ---------------- TAB SWITCHING ----------------

const TAB_RENDERERS = {
    products: renderProductsTab,
    categories: renderCategoriesTab,
    orders: renderOrdersTab,
    users: renderUsersTab
};

function switchTab(tabName) {
    document.querySelectorAll(".admin-tab").forEach(btn => {
        btn.classList.toggle("active", btn.dataset.tab === tabName);
    });
    TAB_RENDERERS[tabName]();
}

document.addEventListener("DOMContentLoaded", () => {
    if (!requireAdmin()) return;

    document.querySelectorAll(".admin-tab").forEach(btn => {
        btn.addEventListener("click", () => switchTab(btn.dataset.tab));
    });

    switchTab("products");
});
