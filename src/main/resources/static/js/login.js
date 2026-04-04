(function () {
  const notice = UI.byId("authNotice");

  async function syncFromServer(messageOnSuccess) {
    try {
      const me = await UI.get("/auth/me");
      APP.setAuth({ email: me.email, roles: me.roles || [] });
      refreshView(messageOnSuccess || "Authenticated server session detected.");
      return;
    } catch (_) {
      APP.clearAuth();
      refreshView("No active server session.");
    }
  }

  function refreshView(message) {
    const auth = APP.getAuth();
    UI.setText("authNotice", message || (auth.email ? "Local session present." : "No local session yet."));
    UI.showJson("authJson", auth);
    UI.applyRoleGuards();
  }

  UI.byId("loginForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        email: UI.byId("loginEmail").value,
        password: UI.byId("loginPassword").value
      };
      await UI.post("/auth/login", payload);
      await syncFromServer("Login successful with active server session.");
    } catch (err) {
      notice.textContent = "Login failed: " + err.message;
    }
  });

  UI.byId("logoutBtn").addEventListener("click", async function () {
    try {
      await UI.post("/auth/logout", {});
    } catch (_) {
      // If logout endpoint returns an error, still clear local cache.
    }
    APP.clearAuth();
    refreshView("Logged out and local session cleared.");
  });

  syncFromServer();
})();
