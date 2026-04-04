window.UI = {
  byId(id) {
    return document.getElementById(id);
  },
  setText(id, value) {
    const el = this.byId(id);
    if (el) {
      el.textContent = value;
    }
  },
  showJson(id, value) {
    const el = this.byId(id);
    if (el) {
      el.textContent = JSON.stringify(value, null, 2);
    }
  },
  async get(path) {
    const response = await fetch(window.APP.apiBase + path, {
      credentials: "include",
      headers: { "Content-Type": "application/json" }
    });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(body.message || body.error || ("HTTP " + response.status));
    }
    return body;
  },
  async post(path, payload) {
    const response = await fetch(window.APP.apiBase + path, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload || {})
    });
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
      throw new Error(body.message || body.error || ("HTTP " + response.status));
    }
    return body;
  },
  applyRoleGuards() {
    const auth = window.APP.getAuth();
    const roles = auth.roles || [];
    const isLoggedIn = !!auth.email;

    document.querySelectorAll("[data-auth-only]").forEach(function (el) {
      if (!isLoggedIn) {
        el.style.display = "none";
      }
    });

    document.querySelectorAll("[data-guest-only]").forEach(function (el) {
      if (isLoggedIn) {
        el.style.display = "none";
      }
    });

    document.querySelectorAll("[data-required-any]").forEach(function (el) {
      const raw = (el.getAttribute("data-required-any") || "").trim();
      const required = raw ? raw.split(",").map(function (r) { return r.trim(); }) : [];
      const allowed = required.some(function (r) { return roles.includes(r); });
      if (!allowed) {
        el.style.display = "none";
      }
    });

    document.querySelectorAll("[data-bind-roles]").forEach(function (el) {
      el.textContent = roles.length ? roles.join(", ") : "GUEST";
    });
  }
};

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", function () {
    window.UI.applyRoleGuards();
  });
} else {
  window.UI.applyRoleGuards();
}
