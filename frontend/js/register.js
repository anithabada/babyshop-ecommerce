document.addEventListener("DOMContentLoaded", () => {
    if (Auth.isLoggedIn()) {
        window.location.href = "index.html";
        return;
    }

    const form = document.getElementById("register-form");
    const alertBox = document.getElementById("form-alert");
    const submitBtn = document.getElementById("submit-btn");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        alertBox.innerHTML = "";
        ["name", "email", "phone", "password"].forEach(f => {
            const el = document.getElementById("err-" + f);
            if (el) el.textContent = "";
        });

        const payload = {
            name: document.getElementById("name").value.trim(),
            email: document.getElementById("email").value.trim(),
            phone: document.getElementById("phone").value.trim(),
            password: document.getElementById("password").value
        };

        submitBtn.disabled = true;
        submitBtn.textContent = "Creating account...";

        try {
            const response = await Api.register(payload);
            Auth.setSession(response);
            alertBox.innerHTML = `<div class="alert alert-success">Account created! Redirecting...</div>`;
            setTimeout(() => window.location.href = "index.html", 700);
        } catch (err) {
            if (err.fieldErrors) {
                Object.entries(err.fieldErrors).forEach(([field, msg]) => {
                    const el = document.getElementById("err-" + field);
                    if (el) el.textContent = msg;
                });
            }
            alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Create Account";
        }
    });
});
