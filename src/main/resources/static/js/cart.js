(function () {
  const tbody = UI.byId("cartTableBody");
  const summaryBox = UI.byId("cartSummary");
  const clearBtn = UI.byId("clearCartBtn");

  async function loadCart() {
    try {
      const { data: cart } = await UI.get("/cart/me");
      
      if (!cart || !cart.items || cart.items.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">Your cart is empty. <a href="/products/view">Browse products</a></td></tr>';
        summaryBox.style.display = "none";
        clearBtn.style.display = "none";
        return;
      }

      summaryBox.style.display = "block";
      clearBtn.style.display = "inline-block";
      UI.setText("cartTotalItems", cart.items.length);
      UI.setText("cartTotalPrice", UI.formatCurrency(cart.cartTotal));

      tbody.innerHTML = cart.items.map(item => `
        <tr>
          <td>
            <strong>${item.productName || 'Product'}</strong>
          </td>
          <td>${UI.formatCurrency(item.unitPrice)}</td>
          <td>
            <div class="qty-form">
              <input type="number" min="1" value="${item.quantity}" data-id="${item.id}" class="qty-input">
              <button class="btn btn-ghost update-qty-btn" style="padding:4px 8px; font-size:12px;" data-id="${item.id}">Update</button>
            </div>
          </td>
          <td><strong>${UI.formatCurrency(item.subtotal)}</strong></td>
          <td class="text-right">
            <button class="btn btn-ghost remove-btn" style="color:var(--danger); border-color:var(--danger); padding:4px 8px;" data-id="${item.id}">X</button>
          </td>
        </tr>
      `).join("");

      attachCartEvents();
    } catch (err) {
      tbody.innerHTML = `<tr><td colspan="5" class="text-center text-muted">Cart load failed: ${err.message}</td></tr>`;
    }
  }

  function attachCartEvents() {
    document.querySelectorAll(".remove-btn").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        try {
          await UI.delete("/cart/me/items/" + e.target.dataset.id);
          UI.toast("Item removed", "success");
          loadCart();
        } catch(err) { UI.toast("Failed to remove: " + err.message, "error"); }
      });
    });

    document.querySelectorAll(".update-qty-btn").forEach(btn => {
      btn.addEventListener("click", async (e) => {
        const id = e.target.dataset.id;
        const input = document.querySelector(`input.qty-input[data-id="${id}"]`);
        try {
          await UI.put("/cart/me/items/" + id + "?quantity=" + input.value);
          UI.toast("Quantity updated", "success");
          loadCart();
        } catch(err) { UI.toast("Update failed: " + err.message, "error"); }
      });
    });
  }

  clearBtn?.addEventListener("click", async () => {
    if(confirm("Are you sure you want to clear your cart?")) {
      try {
        await UI.delete("/cart/me/items");
        UI.toast("Cart cleared", "success");
        loadCart();
      } catch(err) { UI.toast("Failed: " + err.message, "error"); }
    }
  });

  UI.byId("checkoutBtn")?.addEventListener("click", async () => {
    try {
      // Fetch cart items to construct the required OrderRequest payload
      const { data: cart } = await UI.get("/cart/me");
      if (!cart || !cart.items || cart.items.length === 0) {
        throw new Error("Your cart is empty.");
      }

      const orderPayload = {
        items: cart.items.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }))
      };

      // Send the formulated payload to create the order
      await UI.post("/orders", orderPayload);

      // Clear the cart securely after the order completes
      await UI.delete("/cart/me/items");

      UI.toast("Order placed successfully!", "success");
      setTimeout(() => window.location.href = "/orders/view", 1500);
    } catch(err) {
      UI.toast("Checkout failed: " + err.message, "error");
    }
  });

  loadCart();
})();
