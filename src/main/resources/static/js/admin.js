(function () {
  const tbody = UI.byId("adminUsersList");

  async function loadUsers() {
    tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="padding:20px;">Fetching...</td></tr>';
    try {
      const response = await UI.get("/admin/users?page=0&size=50");
      const users = (response.data && response.data.content) || [];

      if (users.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="padding:20px;">No users found.</td></tr>';
        return;
      }

      tbody.innerHTML = users.map(u => {
        const rolesHtml = (u.roles || []).map(r => `<span class="badge badge-${r}">${r}</span>`).join(" ");
        return `
          <tr>
            <td>${u.id}</td>
            <td><strong>${u.fullName || 'N/A'}</strong></td>
            <td>${u.email}</td>
            <td>${rolesHtml}</td>
            <td>
               <span class="text-muted" style="font-size:12px;">Mock Action</span>
            </td>
          </tr>
        `;
      }).join("");
    } catch (err) {
      tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted" style="color:var(--danger)">Failed to load: ${err.message}</td></tr>`;
    }
  }

  UI.byId("refreshUsers")?.addEventListener("click", loadUsers);
  
  // auto load
  if (document.getElementById("adminUsersList")) {
    loadUsers();
  }
})();
