package com.marketplace.dto.order;

import java.util.List;

public record PlaceOrderRequest(List<Item> items) {
    public record Item(Long productId, Integer quantity) {
    }
}
