package com.example.hoddog.service;

import com.example.hoddog.dto.CategoryRequest;
import com.example.hoddog.dto.CategoryResponse;
import com.example.hoddog.entity.Category;
import com.example.hoddog.exception.ResourceNotFoundException;
import com.example.hoddog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse create(CategoryRequest request) {
        Category category = Category.builder()
                .name(request.getName())
                .build();
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category topilmadi"));

        category.setName(request.getName());
        categoryRepository.save(category);
        return mapToResponse(category);
    }

    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category topilmadi");
        }
        categoryRepository.deleteById(id);
    }

    public CategoryResponse get(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category topilmadi"));
        return mapToResponse(category);
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
