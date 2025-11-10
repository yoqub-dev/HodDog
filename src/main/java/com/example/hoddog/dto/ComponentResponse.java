package com.example.hoddog.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ComponentResponse {
    private UUID productId;
    private String productName;
    private Double quantity;
}
