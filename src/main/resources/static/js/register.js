(function () {
  UI.byId("registerForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        fullName: UI.byId("regFullName").value,
        email: UI.byId("regEmail").value,
        password: UI.byId("regPassword").value,
        role: UI.byId("regRole").value
      };
      await UI.post("/auth/register", payload);
      UI.toast("Account created successfully! Redirecting to login...", "success");
      setTimeout(() => window.location.href = "/login", 2000);
    } catch (err) {
      UI.toast("Registration failed: " + err.message, "error");
    }
  });
})();
