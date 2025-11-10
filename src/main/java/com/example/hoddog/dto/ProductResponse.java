package com.example.hoddog.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private Double price;
    private Double quantity;
    private boolean compositeItem;
    private String categoryName;
    private List<ComponentResponse> components;
}
