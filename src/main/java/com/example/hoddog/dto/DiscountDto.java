package com.example.hoddog.dto;

import com.example.hoddog.enums.DiscountType;
import lombok.Data;

@Data
public class DiscountDto {
    private String name;
    private DiscountType type;
    private Double value;
}
