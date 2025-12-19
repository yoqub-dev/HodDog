package com.example.hoddog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private List<String> labels;
    private List<Double> revenue;
    private List<Double> cogs;
    private List<Double> profit;
}
