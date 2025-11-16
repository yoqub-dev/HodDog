package com.example.hoddog.controller;

import com.example.hoddog.dto.CategoryDto;
import com.example.hoddog.entity.Category;
import com.example.hoddog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // CREATE
    @PostMapping
    public Category create(@RequestBody CategoryDto dto) {
        return categoryService.create(dto);
    }

    // GET ALL
    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAll();
    }

    // GET ONE
    @GetMapping("/{id}")
    public Category getById(@PathVariable UUID id) {
        return categoryService.getById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Category update(@PathVariable UUID id,
                           @RequestBody CategoryDto dto) {
        return categoryService.update(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return "Category deleted successfully";
    }
}
