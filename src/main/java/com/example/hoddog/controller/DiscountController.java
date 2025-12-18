package com.example.hoddog.controller;

import com.example.hoddog.dto.DiscountDto;
import com.example.hoddog.dto.SupplierDto;
import com.example.hoddog.entity.Discount;
import com.example.hoddog.entity.Supplier;
import com.example.hoddog.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    public Discount create (@RequestBody DiscountDto discount){
        return discountService.create(discount);
    }

    @PutMapping("/{id}")
    public Discount update(@PathVariable UUID id,
                           @RequestBody DiscountDto dto) {
        return discountService.update(id, dto);
    }

    @GetMapping
    public List<Discount> getAll(){
        return discountService.getAll();
    }

    @GetMapping("/{id}")
    public Discount getById(@PathVariable UUID id){
        return discountService.getById(id);
    }

    @DeleteMapping("/{id}")
    public String  delete(@PathVariable UUID id){
        discountService.delete(id);
        return "Discount deleted";
    }

}
