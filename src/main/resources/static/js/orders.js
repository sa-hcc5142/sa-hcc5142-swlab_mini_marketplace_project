(async function () {
  const container = UI.byId("ordersContainer");

  try {
    const { data: page } = await UI.get("/orders/me?page=0&size=20");
    const orders = page.content || [];

    if (orders.length === 0) {
      container.innerHTML = '<div class="card text-center text-muted">You have no past orders.</div>';
      return;
    }

    container.innerHTML = orders.map(ord => {
      const itemsList = (ord.items || []).map(i => `
        <div class="flex justify-between" style="border-bottom:1px dashed #eee; padding:4px 0;">
          <span style="font-size:13px;">${i.productName || 'Product'} (x${i.quantity})</span>
          <span style="font-size:13px; font-weight:bold;">${UI.formatCurrency(i.subtotal)}</span>
        </div>
      `).join("");

      return `
        <div class="card">
          <div class="flex justify-between items-center mb-1" style="border-bottom:2px solid #f1f5f9; padding-bottom:10px;">
            <h3 style="margin:0;">Order #${ord.id}</h3>
            <span class="badge badge-${ord.status}">${ord.status}</span>
          </div>
          <div class="flex justify-between text-muted mb-2" style="font-size:13px;">
            <span>Date: ${new Date(ord.createdAt).toLocaleDateString()}</span>
            <span>Total: <strong style="color:var(--ink)">${UI.formatCurrency(ord.totalPrice)}</strong></span>
          </div>
          <div style="background:#fafafa; border-radius:6px; padding:10px;">
            ${itemsList}
          </div>
        </div>
      `;
    }).join("");

  } catch (err) {
    container.innerHTML = `<div class="alert alert-error">Failed to load orders: ${err.message}</div>`;
  }
})();
