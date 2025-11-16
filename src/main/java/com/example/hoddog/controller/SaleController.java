package com.example.hoddog.controller;

import com.example.hoddog.dto.SaleRequest;
import com.example.hoddog.entity.Order;
import com.example.hoddog.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public Order createSale(@RequestBody SaleRequest request) {
        return saleService.createSale(request);
    }
}

