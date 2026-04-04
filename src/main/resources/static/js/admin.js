(function () {
  async function loadUsers() {
    try {
      const response = await UI.get("/admin/users?page=0&size=10");
      UI.showJson("adminOutput", response.data || response);
    } catch (err) {
      UI.setText("adminOutput", "Admin API call failed: " + err.message + "\n\nThis endpoint requires ADMIN authorization.");
    }
  }

  UI.byId("refreshUsers").addEventListener("click", loadUsers);
})();
