package com.example.hoddog.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class PurchaseOrderCreateDto {
    private UUID supplierId;
    private LocalDate purchasedDate;
    private String notes;
    private List<PurchaseOrderItemDto> items;
    private List<PurchaseOrderExtraCostDto> extraCosts;
}

