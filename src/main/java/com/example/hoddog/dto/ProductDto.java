package com.example.hoddog.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ProductDto {

    private String name;
    private String description;
    private Boolean availableForSale;
    private String soldBy;          // EACH / WEIGHT
    private Double price;
    private Double cost;            // composite = true bo‘lsa e'tiborsiz
    private String sku;             // bo‘sh bo‘lsa auto-generate qilinadi
    private Boolean composite;
    private Boolean trackStock;
    private Double quantity;
    private Double lowQuantity;

    private UUID categoryId;

    // Composite item ingredient list
    private List<CompositeItemDto> ingredients;

    // Modifier group IDs
    private List<UUID> modifierGroupIds;
}
