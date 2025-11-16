package com.example.hoddog.controller;

import com.example.hoddog.dto.PurchaseOrderCreateDto;
import com.example.hoddog.entity.PurchaseOrder;
import com.example.hoddog.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService poService;

    @PostMapping
    public PurchaseOrder create(@RequestBody PurchaseOrderCreateDto dto) {
        return poService.purchase(dto);
    }


}
