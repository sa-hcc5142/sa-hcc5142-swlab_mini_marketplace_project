window.UI = {
  byId(id) {
    return document.getElementById(id);
  },
  setText(id, value) {
    const el = this.byId(id);
    if (el) el.textContent = value;
  },
  showJson(id, value) {
    const el = this.byId(id);
    if (el) el.textContent = JSON.stringify(value, null, 2);
  },
  formatCurrency(value) {
    return "$" + Number(value).toFixed(2);
  },
  toast(message, type = "success") {
    let container = document.getElementById("toast-container");
    if (!container) {
      container = document.createElement("div");
      container.id = "toast-container";
      document.body.appendChild(container);
    }
    const toast = document.createElement("div");
    toast.className = `alert alert-${type}`;
    toast.innerHTML = `<div>${message}</div> <button onclick="this.parentElement.remove()" style="background:none;border:none;cursor:pointer;opacity:0.7">✕</button>`;
    
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
  },
  async request(method, path, payload) {
    const config = {
      method: method,
      credentials: "include",
      headers: { "Content-Type": "application/json" }
    };
    if (payload) config.body = JSON.stringify(payload);
    
    const response = await fetch(window.APP.apiBase + path, config);
    if (response.status === 204) return null; // No content
    
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
      if (response.status === 401 || response.status === 403) {
        const path = window.location.pathname;
        if (path !== "/api/login" && path !== "/api/register" && path !== "/login" && path !== "/register") {
          setTimeout(() => window.location.href = "/api/login", 2000);
        }
        throw new Error(body.message || "Unauthorized. Please login again.");
      }
      throw new Error(body.message || body.error || ("HTTP " + response.status));
    }
    return body;
  },
  async get(path) { return this.request("GET", path); },
  async post(path, payload) { return this.request("POST", path, payload); },
  async put(path, payload) { return this.request("PUT", path, payload); },
  async delete(path) { return this.request("DELETE", path); },
  
  applyRoleGuards() {
    const auth = window.APP.getAuth();
    const roles = auth.roles || [];
    const isLoggedIn = !!auth.email;

    document.querySelectorAll("[data-auth-only]").forEach(el => {
      el.style.display = isLoggedIn ? "" : "none";
    });

    document.querySelectorAll("[data-guest-only]").forEach(el => {
      el.style.display = !isLoggedIn ? "" : "none";
    });

    document.querySelectorAll("[data-required-any]").forEach(el => {
      const raw = (el.getAttribute("data-required-any") || "").trim();
      const required = raw ? raw.split(",").map(r => r.trim()) : [];
      const allowed = required.some(r => roles.includes(r));
      if (!allowed) el.style.display = "none";
    });

    // Binding auth info directly to DOM
    document.querySelectorAll("[data-bind-email]").forEach(el => {
      el.textContent = auth.email || "Guest";
    });
    document.querySelectorAll("[data-bind-roles]").forEach(el => {
      if (roles.length) {
        el.innerHTML = roles.map(r => `<span class="badge badge-${r}">${r}</span>`).join(" ");
      } else {
        el.textContent = "GUEST";
      }
    });
  }
};

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", () => window.UI.applyRoleGuards());
} else {
  window.UI.applyRoleGuards();
}
