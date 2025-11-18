package com.example.hoddog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfitReportDto {

    private String productName;
    private Long quantity;
    private Double totalSales;
    private Double totalCost;
    private Double profit;
    private Double margin;
}
