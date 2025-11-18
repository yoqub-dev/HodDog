package com.example.hoddog.controller;

import com.example.hoddog.dto.ProfitReportDto;
import com.example.hoddog.service.ProfitReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ProfitReportController {

    private final ProfitReportService profitReportService;

    @GetMapping("/profit")
    public List<ProfitReportDto> getProfitReport(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return profitReportService.getProfit(start, end);
    }
}
