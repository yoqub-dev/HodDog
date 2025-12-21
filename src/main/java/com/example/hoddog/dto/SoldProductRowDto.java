package com.example.hoddog.dto;

public record SoldProductRowDto(

                                String sku,
                                String productName,
                                Long totalOrder,
                                Double totalAmount
)
{
}
