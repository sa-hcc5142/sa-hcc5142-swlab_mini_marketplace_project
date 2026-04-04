window.APP = {
  apiBase: "/api",
  getAuth() {
    try {
      return JSON.parse(localStorage.getItem("mm_auth") || "{}");
    } catch (e) {
      return {};
    }
  },
  setAuth(data) {
    localStorage.setItem("mm_auth", JSON.stringify(data || {}));
  },
  clearAuth() {
    localStorage.removeItem("mm_auth");
  },
  hasRole(role) {
    const auth = this.getAuth();
    const roles = auth.roles || [];
    return roles.includes(role);
  }
};
