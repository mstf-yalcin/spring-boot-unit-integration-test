package com.example.test.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record UpdateProductDto(@NotBlank String id,
                               @NotBlank String name,
                               @NotBlank String description,
                               @Min(1) double price,
                               @Min(1) int stockQuantity) {
}
