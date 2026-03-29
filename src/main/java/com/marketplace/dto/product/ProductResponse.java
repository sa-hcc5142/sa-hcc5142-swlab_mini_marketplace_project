package com.marketplace.dto.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductResponse(
    Long id,
    String productName,
    String description,
    Double price,
    Integer stock,
    String category,
    Long sellerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
