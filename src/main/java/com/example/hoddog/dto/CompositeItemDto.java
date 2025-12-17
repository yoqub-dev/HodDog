package com.example.hoddog.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CompositeItemDto {
    private UUID ingredientProductId;
    private Double quantity;
}
