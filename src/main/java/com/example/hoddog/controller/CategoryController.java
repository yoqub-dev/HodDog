package com.example.hoddog.controller;

import com.example.hoddog.dto.CategoryRequest;
import com.example.hoddog.dto.CategoryResponse;
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

    @PostMapping
    public CategoryResponse create(@RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @RequestBody CategoryRequest request) {
        return categoryService.update(id, request);
    }

    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable UUID id) {
        return categoryService.get(id);
    }

    @GetMapping
    public List<CategoryResponse> getAll() {
        return categoryService.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        categoryService.delete(id);
    }
}
