(async function () {
  const pdName = UI.byId("pdName");
  const id = UI.byId("productIdFromView")?.value || new URLSearchParams(window.location.search).get("id");

  if (!id || id === "0" || id === 0) {
    pdName.textContent = "Invalid Product ID";
    return;
  }

  async function loadDetails() {
    try {
      const { data: p } = await UI.get("/products/" + id);
      UI.setText("pdName", p.productName);
      UI.setText("pdCategory", p.category);
      UI.setText("pdStock", "Stock: " + p.stock);
      UI.setText("pdDesc", p.description);
      UI.setText("pdPrice", UI.formatCurrency(p.price));
      
      const { data: rating } = await UI.get("/products/" + id + "/reviews/rating/average");
      UI.setText("pdAvgRating", "Avg Rating: " + (rating > 0 ? Number(rating).toFixed(1) : "N/A") + " ⭐");
    } catch(err) {
      pdName.textContent = "Unable to load product.";
      UI.toast(err.message, "error");
    }
  }

  async function loadReviews() {
    try {
      const { data: page } = await UI.get("/products/" + id + "/reviews?page=0&size=50");
      const list = UI.byId("reviewsList");
      if (!page.content || page.content.length === 0) {
        list.innerHTML = '<div class="text-muted" style="padding:10px;">No reviews yet.</div>';
        return;
      }
      
      const auth = APP.getAuth();
      const roles = auth.roles || [];
      const isAdmin = roles.includes("ROLE_ADMIN") || roles.includes("ADMIN");
      const currentUserEmail = auth.email;

      list.innerHTML = page.content.map(r => `
        <div style="border-bottom: 1px solid #f1f5f9; padding:10px 0;">
          <div class="flex justify-between items-center mb-1">
            <strong>${r.buyerUsername || "Anonymous"}</strong>
            <div>
              <span class="badge">Rating: ${r.rating} ⭐</span>
              ${(isAdmin || r.buyerUsername === currentUserEmail) ? `<button class="btn btn-ghost del-review-btn" style="color:red; border:1px solid red; padding:2px 6px; font-size:12px; margin-left:10px;" data-id="${r.id}">Delete</button>` : ''}
            </div>
          </div>
          <p style="margin:0; font-size:14px;">${r.comment}</p>
        </div>
      `).join("");
      
      // Attach delete events
      document.querySelectorAll(".del-review-btn").forEach(btn => {
        btn.addEventListener("click", async function(e) {
          if (!confirm("Are you sure you want to delete this review?")) return;
          try {
            await UI.delete("/products/" + id + "/reviews/" + e.target.dataset.id);
            UI.toast("Review deleted", "success");
            loadReviews();
            loadDetails(); // To update the average rating
          } catch (err) {
            UI.toast("Delete failed. " + err.message, "error");
          }
        });
      });
      
    } catch(err) {
      console.error("Failed to load reviews:", err);
    }
  }

  UI.byId("addToCartForm")?.addEventListener("submit", async function(e) {
    e.preventDefault();
    try {
      await UI.post("/cart/me/items", {
        productId: Number(id),
        quantity: Number(UI.byId("pdQuantity").value || 1)
      });
      UI.toast("Added to Cart!", "success");
    } catch(err) {
      UI.toast("Failed to add: " + err.message, "error");
    }
  });

  UI.byId("reviewForm")?.addEventListener("submit", async function(e) {
    e.preventDefault();
    try {
      await UI.post("/products/" + id + "/reviews", {
        rating: Number(UI.byId("reviewRating").value),
        comment: UI.byId("reviewComment").value
      });
      UI.toast("Review submitted!", "success");
      UI.byId("reviewComment").value = "";
      loadReviews();
      loadDetails(); // reload average rating
    } catch(err) {
      UI.toast("Review failed: " + err.message, "error");
    }
  });

  loadDetails();
  loadReviews();
})();
