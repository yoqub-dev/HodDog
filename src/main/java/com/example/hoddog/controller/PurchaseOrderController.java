package com.example.hoddog.controller;

import com.example.hoddog.dto.PurchaseOrderCreateDto;
import com.example.hoddog.dto.PurchaseReportDto;
import com.example.hoddog.entity.PurchaseOrder;
import com.example.hoddog.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    public PurchaseOrder create(@RequestBody PurchaseOrderCreateDto dto) {
        return purchaseOrderService.purchase(dto);
    }

    @GetMapping
    public List<PurchaseReportDto> purchases(
            @RequestParam(defaultValue = "day") String period, // day/week/month/year
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        return purchaseOrderService.getPurchases(period, start, end);
    }


}
