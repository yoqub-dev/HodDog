package com.example.hoddog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProfitExpenseChartDto {

    private List<String> labels;

    // 1-chiziq: harajat (COGS)
    private List<Double> expense;

    // 2-chiziq: foyda
    private List<Double> profit;
}
