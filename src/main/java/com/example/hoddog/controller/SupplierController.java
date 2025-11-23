package com.example.hoddog.controller;

import com.example.hoddog.dto.SupplierDto;
import com.example.hoddog.entity.Supplier;
import com.example.hoddog.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/supplierss")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public Supplier create(@RequestBody SupplierDto dto) {
        return supplierService.create(dto);
    }

    @GetMapping
    public List<Supplier> getAll() {
        return supplierService.getAll();
    }

    @GetMapping("/{id}")
    public Supplier getById(@PathVariable UUID id) {
        return supplierService.getById(id);
    }

    @PutMapping("/{id}")
    public Supplier update(@PathVariable UUID id,
                           @RequestBody SupplierDto dto) {
        return supplierService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return "Supplier deleted successfully";
    }
}

