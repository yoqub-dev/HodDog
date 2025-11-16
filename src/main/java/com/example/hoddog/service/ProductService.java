package com.example.hoddog.service;

import com.example.hoddog.dto.CompositeItemDto;
import com.example.hoddog.dto.ProductDto;
import com.example.hoddog.entity.*;
import com.example.hoddog.enums.SoldBy;
import com.example.hoddog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModifierRepository modifierRepository;
    private final ProductRepository repo;

    // CREATE
    public Product create(ProductDto dto) {

        // SKU AUTO GENERATE
        if (dto.getSku() == null || dto.getSku().isEmpty()) {
            dto.setSku(generateSku());
        }

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .availableForSale(dto.getAvailableForSale())
                .soldBy(SoldBy.valueOf(dto.getSoldBy()))
                .price(dto.getPrice())
                .cost(dto.getCost())
                .sku(dto.getSku())
                .composite(dto.getComposite())
                .trackStock(dto.getTrackStock())
                .quantity(dto.getQuantity())
                .lowQuantity(dto.getLowQuantity())
                .build();

        // Category ulash
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        // Save product so composite can reference it
        product = productRepository.save(product);

        // Composite item ingredientlar
        if (Boolean.TRUE.equals(dto.getComposite()) && dto.getIngredients() != null) {
            for (CompositeItemDto itemDto : dto.getIngredients()) {

                Product ingredient = productRepository.findById(itemDto.getIngredientProductId())
                        .orElseThrow(() -> new RuntimeException("Ingredient product not found"));

                CompositeItem compositeItem = CompositeItem.builder()
                        .parentProduct(product)
                        .ingredientProduct(ingredient)
                        .quantity(itemDto.getQuantity())
                        .build();

                product.getIngredients().add(compositeItem);
            }

            // cost auto-calculation
            double totalCost = calculateCompositeCost(product);
            product.setCost(totalCost);
        }

        // ModifierGroups ulash
        if (dto.getModifierGroupIds() != null) {
            List<Modifier> groups = modifierRepository.findAllById(dto.getModifierGroupIds());
            product.setModifierGroups(groups);
        }

        return productRepository.save(product);
    }

    // Composite product cost auto calculation
    private double calculateCompositeCost(Product product) {
        return product.getIngredients().stream()
                .mapToDouble(i -> i.getIngredientProduct().getCost() * i.getQuantity())
                .sum();
    }

    // SKU AUTO-GENERATE (1001, 1002, 1003...)
    private String generateSku() {
        String maxSku = repo.findMaxSku();
        if (maxSku == null) return "1001";
        int next = Integer.parseInt(maxSku) + 1;
        return String.valueOf(next);
    }

    // GET ALL
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // GET ONE
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}
