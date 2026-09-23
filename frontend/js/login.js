document.addEventListener("DOMContentLoaded", () => {
    if (Auth.isLoggedIn()) {
        window.location.href = "index.html";
        return;
    }

    const form = document.getElementById("login-form");
    const alertBox = document.getElementById("form-alert");
    const submitBtn = document.getElementById("submit-btn");

    const params = new URLSearchParams(window.location.search);
    const redirectTo = params.get("redirect");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        alertBox.innerHTML = "";

        const payload = {
            email: document.getElementById("email").value.trim(),
            password: document.getElementById("password").value
        };

        submitBtn.disabled = true;
        submitBtn.textContent = "Logging in...";

        try {
            const response = await Api.login(payload);
            Auth.setSession(response);
            window.location.href = redirectTo && redirectTo !== "login.html" ? redirectTo : "index.html";
        } catch (err) {
            alertBox.innerHTML = `<div class="alert alert-error">${escapeHtml(err.message)}</div>`;
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Log In";
        }
    });
});
