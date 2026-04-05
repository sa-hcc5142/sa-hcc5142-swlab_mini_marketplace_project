(function () {
  const output = UI.byId("sellerOutput");

  UI.byId("sellerForm").addEventListener("submit", async function (e) {
    e.preventDefault();
    try {
      const payload = {
        productName: UI.byId("name").value,
        description: UI.byId("description").value,
        price: Number(UI.byId("price").value),
        stock: Number(UI.byId("stock").value),
        category: UI.byId("category").value
      };

      const response = await UI.post("/products", payload);
      UI.showJson("sellerOutput", response);
    } catch (err) {
      output.textContent = "Create product failed: " + err.message;
    }
  });
})();
