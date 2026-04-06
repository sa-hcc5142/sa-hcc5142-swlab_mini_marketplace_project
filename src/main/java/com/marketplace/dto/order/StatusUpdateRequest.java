package com.marketplace.dto.order;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status
) {}
