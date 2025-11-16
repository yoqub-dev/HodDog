package com.example.hoddog.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class PurchaseOrderItemDto {
    private UUID productId;
    private Double quantity;
    private Double purchaseCost;
}
