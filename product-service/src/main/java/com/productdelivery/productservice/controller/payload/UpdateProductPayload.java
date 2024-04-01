package com.productdelivery.productservice.controller.payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductPayload(
        @NotNull(message = "{catalog.products.update.errors.title_is_null}")
        @Size(min = 3, max = 50, message = "{catalog.products.update.errors.title_size_is_invalid}")
        String title,
        @Size(max = 1000, message = "{catalog.products.update.errors.details_size_is_invalid}")
        String details) {
}
