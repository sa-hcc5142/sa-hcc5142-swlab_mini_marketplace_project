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
               <button class="btn btn-ghost del-user-btn" style="color:var(--danger); border-color:var(--danger); padding:4px 8px; font-size:12px;" data-id="${u.id}">Deactivate</button>
            </td>
          </tr>
        `;
      }).join("");

      document.querySelectorAll(".del-user-btn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
          if (!confirm("Deactivate this user?")) return;
          try {
            await UI.delete("/admin/users/" + e.target.dataset.id);
            UI.toast("User deactivated", "success");
            loadUsers();
          } catch(err) {
            UI.toast("Deactivation failed: " + err.message, "error");
          }
        });
      });
    } catch (err) {
      tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted" style="color:var(--danger)">Failed to load: ${err.message}</td></tr>`;
    }
  }

  UI.byId("refreshUsers")?.addEventListener("click", loadUsers);
  
  const oBody = UI.byId("adminOrdersList");
  async function loadOrders() {
    if(!oBody) return;
    oBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="padding:20px;">Fetching orders...</td></tr>';
    try {
      const response = await UI.get("/orders/admin?page=0&size=50");
      const orders = (response.data && response.data.content) || [];

      if (orders.length === 0) {
        oBody.innerHTML = '<tr><td colspan="5" class="text-center text-muted" style="padding:20px;">No orders found.</td></tr>';
        return;
      }

      oBody.innerHTML = orders.map(o => `
          <tr>
            <td>${o.id}</td>
            <td>${o.buyerEmail || 'N/A'}</td>
            <td>${UI.formatCurrency(o.totalPrice)}</td>
            <td>
              <select class="status-select" data-id="${o.id}">
                <option value="PENDING" ${o.status === "PENDING" ? "selected" : ""}>PENDING</option>
                <option value="SHIPPED" ${o.status === "SHIPPED" ? "selected" : ""}>SHIPPED</option>
                <option value="DELIVERED" ${o.status === "DELIVERED" ? "selected" : ""}>DELIVERED</option>
                <option value="CANCELLED" ${o.status === "CANCELLED" ? "selected" : ""}>CANCELLED</option>
              </select>
            </td>
            <td>
               <button class="btn btn-ghost save-status-btn" style="color:var(--brand); border-color:var(--brand); padding:4px 8px; font-size:12px;" data-id="${o.id}">Save</button>
            </td>
          </tr>
      `).join("");

      document.querySelectorAll(".save-status-btn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
          const id = e.target.dataset.id;
          const status = document.querySelector(`.status-select[data-id="${id}"]`).value;
          try {
            await UI.put("/orders/" + id + "/status?status=" + status, {});
            UI.toast("Order status updated", "success");
            loadOrders();
          } catch(err) {
            UI.toast("Failed to update status: " + err.message, "error");
          }
        });
      });
    } catch (err) {
      oBody.innerHTML = `<tr><td colspan="5" class="text-center text-muted" style="color:var(--danger)">Failed to load orders: ${err.message}</td></tr>`;
    }
  }

  UI.byId("refreshOrders")?.addEventListener("click", loadOrders);

  // auto load
  if (document.getElementById("adminUsersList")) {
    loadUsers();
    loadOrders();
  }
})();
