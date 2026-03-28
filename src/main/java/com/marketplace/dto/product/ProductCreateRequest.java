package com.marketplace.dto.product;

import java.math.BigDecimal;

public record ProductCreateRequest(
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String category
) {
}
