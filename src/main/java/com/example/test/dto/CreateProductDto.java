package com.example.test.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProductDto(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull() @DecimalMin("0.1") double price,
        @Min(value = 1) int stockQuantity
) {}
