package com.example.hoddog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyProfitDto {
    private LocalDate date;
    private Double totalSales;
    private Double totalCost;
    private Double profit;
    private Double margin;
}
