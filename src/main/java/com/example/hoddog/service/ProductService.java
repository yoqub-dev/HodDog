package com.example.hoddog.service;

import com.example.hoddog.dto.CompositeItemDto;
import com.example.hoddog.dto.ProductDto;
import com.example.hoddog.entity.*;
import com.example.hoddog.enums.SoldBy;
import com.example.hoddog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModifierRepository modifierRepository;
    private final FileStorageService fileStorageService;


    // CREATE
    @Transactional
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

        // Saqlaymiz — composite bog'lanishi uchun kerak
        product = productRepository.save(product);


        if (product.getIngredients() == null) {
            product.setIngredients(new ArrayList<>());
        }

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

            // Cost auto calculation
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


    // UPDATE
    @Transactional
    public Product update(UUID productId, ProductDto dto) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Oddiy fieldlar
        if (dto.getName() != null) {
            product.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            product.setDescription(dto.getDescription());
        }
        if (dto.getAvailableForSale() != null) {
            product.setAvailableForSale(dto.getAvailableForSale());
        }
        if (dto.getSoldBy() != null) {
            product.setSoldBy(SoldBy.valueOf(dto.getSoldBy()));
        }
        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }

        // SKU – agar jo'natilsa, yangilanadi; bo'sh bo'lsa eskicha qoladi
        if (dto.getSku() != null && !dto.getSku().isBlank()) {
            product.setSku(dto.getSku());
        }

        // Track stock bilan quantity'lar
        if (dto.getTrackStock() != null) {
            product.setTrackStock(dto.getTrackStock());
        }
        if (dto.getQuantity() != null) {
            product.setQuantity(dto.getQuantity());
        }
        if (dto.getLowQuantity() != null) {
            product.setLowQuantity(dto.getLowQuantity());
        }

        // Category
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Composite logika
        if (dto.getComposite() != null) {
            product.setComposite(dto.getComposite());

            // Avvalgi ingredientlarni tozalaymiz (orphanRemoval = true)
            if (product.getIngredients() != null) {
                product.getIngredients().clear();
            } else {
                product.setIngredients(new ArrayList<>());
            }

            // Agar composite true bo'lsa va yangi ingredients kelgan bo'lsa
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

                // Auto cost hisoblash composite uchun
                double totalCost = calculateCompositeCost(product);
                product.setCost(totalCost);
            } else {
                // Oddiy product bo'lsa cost DTO'dan olinadi
                if (dto.getCost() != null) {
                    product.setCost(dto.getCost());
                }
            }
        } else {
            // composite field umuman jo'natilmasa, faqat cost ni yangilash (agar kelgan bo'lsa)
            if (dto.getCost() != null) {
                product.setCost(dto.getCost());
            }
        }

        // Modifier group'lar
        if (dto.getModifierGroupIds() != null) {
            if (dto.getModifierGroupIds().isEmpty()) {
                product.getModifierGroups().clear();
            } else {
                List<Modifier> groups = modifierRepository.findAllById(dto.getModifierGroupIds());
                product.setModifierGroups(groups);
            }
        }

        return productRepository.save(product);
    }



    @Transactional
    public Product uploadImage(UUID productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Eski rasmni o'chirish
        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }

        // Yangi rasmni saqlash
        String imageUrl = fileStorageService.storeFile(file);
        product.setImageUrl(imageUrl);

        return productRepository.save(product);
    }

    // 🖼️ YANGI: Rasmni o'chirish
    @Transactional
    public Product deleteImage(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
            product.setImageUrl(null);
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
    public String generateSku() {

        String lastSku = productRepository.findMaxSku();

        if (lastSku == null || lastSku.isBlank()) {
            return "1001";
        }
        try {
            return String.valueOf(Integer.parseInt(lastSku) + 1);
        } catch (Exception e) {
            return "1001";
        }
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

    public List<Product> universalSearch(String value) {
        if (value == null || value.isBlank()) {
            return getAll();
        }
        return productRepository
                .findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(value, value);
    }

    @Transactional
    public void delete(UUID productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }

        productRepository.delete(product);
    }

    // ProductService ichiga qo‘shing
    @Transactional
    public void recalcCompositeCostsForProducts(List<UUID> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        for (Product p : products) {
            // faqat composite va ingredientlari bor bo‘lsa
            if (p.isComposite() && p.getIngredients() != null) {
                double totalCost = p.getIngredients().stream()
                        .mapToDouble(ci -> ci.getIngredientProduct().getCost() * ci.getQuantity())
                        .sum();
                p.setCost(totalCost);
                productRepository.save(p);
            }
        }
    }
}