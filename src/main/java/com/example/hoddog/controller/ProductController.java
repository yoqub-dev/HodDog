package com.example.hoddog.controller;

import com.example.hoddog.dto.ProductDto;
import com.example.hoddog.entity.Product;
import com.example.hoddog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public Product create(@RequestBody ProductDto dto) {
        return productService.create(dto);
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public Product getOne(@PathVariable UUID id) {
        return productService.getById(id);
    }
}
