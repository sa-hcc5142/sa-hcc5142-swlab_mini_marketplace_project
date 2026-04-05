(function () {
  const notice = UI.byId("authNotice");

  async function syncFromServer() {
    try {
      const me = await UI.get("/auth/me");
      APP.setAuth({ email: me.email, roles: me.roles || [] });
      window.location.href = "/api/dashboard"; // Redirect on existing session
    } catch (_) {
      APP.clearAuth();
    }
  }

  UI.byId("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        email: UI.byId("loginEmail").value,
        password: UI.byId("loginPassword").value
      };
      await UI.post("/auth/login", payload);
      // Wait to populate the local session, then redirect
      const me = await UI.get("/auth/me");
      APP.setAuth({ email: me.email, roles: me.roles || [] });
      window.location.href = "/api/dashboard";
    } catch (err) {
      UI.toast("Login failed: " + err.message, "error");
    }
  });

  // Check if already logged in on page load
  syncFromServer();
})();
