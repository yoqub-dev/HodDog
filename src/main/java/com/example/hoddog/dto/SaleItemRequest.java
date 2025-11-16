package com.example.hoddog.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SaleItemRequest {

    private UUID productId;
    private Integer quantity;

    private List<UUID> modifierOptionIds;
}

