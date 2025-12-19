package com.example.hoddog.controller;

import com.example.hoddog.dto.AnalyticsSummaryDto;
import com.example.hoddog.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // /api/analytics/summary?mode=week
    // /api/analytics/summary?mode=month
    // optional: &start=2025-12-01&end=2025-12-31
    @GetMapping("/summary")
    public AnalyticsSummaryDto summary(
            @RequestParam(defaultValue = "week") String mode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return analyticsService.getSummary(mode, start, end);
    }
}
