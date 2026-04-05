(async function () {
  const list = UI.byId("productsList");
  const errorBox = UI.byId("productsError");

  try {
    const response = await UI.get("/products?page=0&size=20");
    const products = (((response || {}).data || {}).content || []);

    if (!products.length) {
      list.innerHTML = '<div class="card">No products found.</div>';
      return;
    }

    list.innerHTML = products.map(function (p) {
      return `
        <article class="card flex" style="flex-direction:column; justify-content:space-between;">
          <div style="margin-bottom:12px;">
            <div class="flex justify-between items-center mb-1">
              <h3 style="margin:0">${p.productName || "Unnamed Product"}</h3>
              <span class="badge">${UI.formatCurrency(p.price)}</span>
            </div>
            <p class="text-muted" style="font-size:14px; margin:4px 0">${p.category || "General"}</p>
            <p class="text-muted" style="font-size:13px; margin:0">Stock: ${p.stock ?? "n/a"}</p>
          </div>
          <div>
            <a class="btn btn-primary w-full text-center" style="display:block; text-decoration:none;" href="/api/products/view/${p.id}">View Details</a>
          </div>
        </article>
      `;
    }).join("");
  } catch (err) {
    errorBox.style.display = "block";
    errorBox.textContent = "Unable to load products: " + err.message;
  }
})();
