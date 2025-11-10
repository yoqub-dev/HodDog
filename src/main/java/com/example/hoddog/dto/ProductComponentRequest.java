package com.example.hoddog.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ProductComponentRequest {
    private UUID productId;
    private Double quantity;
}

