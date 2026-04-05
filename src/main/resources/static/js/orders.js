(async function () {
  try {
    const response = await UI.get("/orders/me?page=0&size=10");
    UI.showJson("ordersOutput", response.data || response);
  } catch (err) {
    UI.setText("ordersOutput", "Orders API call failed: " + err.message + "\n\nThis endpoint requires BUYER or ADMIN authorization.");
  }
})();
