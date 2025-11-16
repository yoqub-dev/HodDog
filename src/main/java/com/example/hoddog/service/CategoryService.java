package com.example.hoddog.service;

import com.example.hoddog.dto.CategoryDto;
import com.example.hoddog.entity.Category;
import com.example.hoddog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // CREATE
    public Category create(CategoryDto dto) {

        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Category already exists: " + dto.getName());
        }

        Category category = Category.builder()
                .name(dto.getName())
                .build();

        return categoryRepository.save(category);
    }

    // GET ALL
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    // GET BY ID
    public Category getById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    // UPDATE
    public Category update(UUID id, CategoryDto dto) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Agar nomni o‘zgartirmoqchi bo‘lsa va mavjud bo‘lsa error
        if (!category.getName().equalsIgnoreCase(dto.getName()) &&
                categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }

        category.setName(dto.getName());
        return categoryRepository.save(category);
    }

    // DELETE
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Category not found");
        }
        categoryRepository.deleteById(id);
    }
}
