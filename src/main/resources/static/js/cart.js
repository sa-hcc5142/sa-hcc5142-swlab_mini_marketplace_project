(async function () {
  try {
    const response = await UI.get("/cart/me");
    UI.showJson("cartOutput", response.data || response);
  } catch (err) {
    UI.setText("cartOutput", "Cart API call failed: " + err.message + "\n\nLogin/session setup may be required for protected endpoints.");
  }
})();
