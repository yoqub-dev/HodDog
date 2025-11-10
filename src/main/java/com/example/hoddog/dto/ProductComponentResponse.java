package com.example.hoddog.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ProductComponentResponse {
    private UUID id;
    private UUID childProductId;
    private String childProductName;
    private Double quantity;
}
