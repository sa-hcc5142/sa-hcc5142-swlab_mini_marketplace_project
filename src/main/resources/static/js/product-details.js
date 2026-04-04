(async function () {
  const params = new URLSearchParams(window.location.search);
  const idFromView = document.getElementById("productIdFromView");
  const id = (idFromView && idFromView.value) || params.get("id");

  if (!id) {
    UI.setText("productDetails", "Missing query parameter: id");
    return;
  }

  try {
    const response = await UI.get("/products/" + id);
    UI.showJson("productDetails", response.data || response);
  } catch (err) {
    const box = UI.byId("detailError");
    box.style.display = "block";
    box.textContent = "Unable to load product: " + err.message;
    UI.setText("productDetails", "");
  }
})();
