package com.example.hoddog.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseReportDto(
        UUID productId,
        String productName,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        Long quantity,
        Double totalCost
) {}