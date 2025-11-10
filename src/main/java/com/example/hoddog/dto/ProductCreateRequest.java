package com.example.hoddog.dto;

import com.example.hoddog.enums.SoldBy;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private String sku;
    private String barcode;
    private Double price;
    private Double cost;
    private SoldBy soldBy;
    private boolean compositeItem;
    private boolean trackStock;
    private Double quantity;
    private UUID categoryId;

    private List<ComponentRequest> components;
}
