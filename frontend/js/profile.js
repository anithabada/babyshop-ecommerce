async function loadProfile() {
    const alertBox = document.getElementById("form-alert");
    try {
        const profile = await Api.getMyProfile();
        document.getElementById("email").value = profile.email;
        document.getElementById("name").value = profile.name;
        document.getElementById("phone").value = profile.phone;
        document.getElementById("role").value = profile.role === "ROLE_ADMIN" ? "Administrator" : "Customer";
    } catch (e) {
        alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(e.message)}</div>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    if (!requireAuth()) return;
    loadProfile();

    const form = document.getElementById("profile-form");
    const alertBox = document.getElementById("form-alert");
    const submitBtn = document.getElementById("submit-btn");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        alertBox.innerHTML = "";

        const payload = {
            name: document.getElementById("name").value.trim(),
            phone: document.getElementById("phone").value.trim()
        };

        submitBtn.disabled = true;
        submitBtn.textContent = "Saving...";

        try {
            const updated = await Api.updateMyProfile(payload);
            // keep localStorage user info in sync (e.g. navbar greeting)
            const stored = Auth.getUser();
            stored.name = updated.name;
            localStorage.setItem("babyshop_user", JSON.stringify(stored));

            alertBox.innerHTML = `<div class="alert alert-success">Profile updated successfully.</div>`;
            renderNavbar();
        } catch (err) {
            alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Save Changes";
        }
    });
});
