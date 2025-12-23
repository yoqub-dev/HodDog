package com.example.hoddog.controller;

import com.example.hoddog.dto.DailyProfitDto;
import com.example.hoddog.service.DailyProfitService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class DailyProfitController {

    private final DailyProfitService dailyProfitService;

    @GetMapping("/daily-profit")
    public List<DailyProfitDto> getDailyProfit(
            @RequestParam(value = "start", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,

            @RequestParam(value = "end", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end
    ) {
        return dailyProfitService.getDaily(start, end);
    }

}