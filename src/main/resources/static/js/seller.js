(function () {
  const tbody = UI.byId("sellerProductsList");

  async function loadProducts() {
    try {
      const { data: page } = await UI.get("/products?page=0&size=50");
      const products = page.content || [];

      if (products.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">No products recorded.</td></tr>';
        return;
      }

      tbody.innerHTML = products.map(p => `
        <tr>
          <td>${p.id}</td>
          <td><strong>${p.productName || 'Unnamed'}</strong><br><span style="font-size:12px;" class="text-muted">${p.category}</span></td>
          <td>${UI.formatCurrency(p.price)}</td>
          <td>${p.stock}</td>
          <td>
            <button class="btn btn-ghost del-prod-btn" style="color:var(--danger); border-color:var(--danger); padding:4px 8px; font-size:12px;" data-id="${p.id}">Delete</button>
          </td>
        </tr>
      `).join("");

      document.querySelectorAll(".del-prod-btn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
          if (!confirm("Delete product?")) return;
          try {
            await UI.delete("/products/" + e.target.dataset.id);
            UI.toast("Product deleted", "success");
            loadProducts();
          } catch(err) {
            UI.toast("Delete failed. You might not be the owner. " + err.message, "error");
          }
        });
      });
    } catch (err) {
      tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Load failed: ${err.message}</td></tr>`;
    }
  }

  UI.byId("sellerForm")?.addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        productName: UI.byId("name").value,
        description: UI.byId("description").value,
        price: Number(UI.byId("price").value),
        stock: Number(UI.byId("stock").value),
        category: UI.byId("category").value
      };

      await UI.post("/products", payload);
      UI.toast("Product created successfully!", "success");
      UI.byId("sellerForm").reset();
      loadProducts();
    } catch (err) {
      UI.toast("Creation failed: " + err.message, "error");
    }
  });

  loadProducts();
})();
