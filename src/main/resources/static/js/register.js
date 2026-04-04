(function () {
  const notice = UI.byId("registerNotice");

  UI.byId("registerForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        fullName: UI.byId("regFullName").value,
        email: UI.byId("regEmail").value,
        password: UI.byId("regPassword").value,
        role: UI.byId("regRole").value
      };
      const data = await UI.post("/auth/register", payload);
      APP.setAuth({ email: data.email, roles: data.roles || [] });
      notice.textContent = "Registration successful. You can continue to login or browse products.";
      UI.applyRoleGuards();
    } catch (err) {
      notice.textContent = "Register failed: " + err.message;
    }
  });
})();
