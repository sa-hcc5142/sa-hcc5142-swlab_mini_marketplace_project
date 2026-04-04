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
        <article class="list-item">
          <div>
            <h3>${p.productName || "Unnamed Product"}</h3>
            <p class="kv">${p.category || "General"} • Stock: ${p.stock ?? "n/a"}</p>
            <p class="kv">$${p.price ?? "0.00"}</p>
          </div>
          <div>
            <a class="btn btn-primary" href="/api/products/view/${p.id}">Details</a>
          </div>
        </article>
      `;
    }).join("");
  } catch (err) {
    errorBox.style.display = "block";
    errorBox.textContent = "Unable to load products: " + err.message;
  }
})();
