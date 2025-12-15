package com.example.hoddog.controller;

import com.example.hoddog.dto.ProductSalesReportDto;
import com.example.hoddog.dto.SaleRequest;
import com.example.hoddog.entity.Order;
import com.example.hoddog.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sale")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public Order createSale(@RequestBody SaleRequest request) {
        return saleService.createSale(request);
    }

    @GetMapping
    public List<ProductSalesReportDto> sales(
            @RequestParam(defaultValue = "day") String period, // day/week/month/year
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return saleService.getSales(period, start, end);
    }
}

