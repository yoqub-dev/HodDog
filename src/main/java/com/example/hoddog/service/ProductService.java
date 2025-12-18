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

    // ✅ CREATE (name duplicate bo'lmasin + inactive bo'lsa reactivate)
    @Transactional
    public Product create(ProductDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new RuntimeException("Name is required");
        }

        // SKU AUTO GENERATE
        if (dto.getSku() == null || dto.getSku().isBlank()) {
            dto.setSku(generateSku());
        }

        // ✅ oldingi product bormi?
        Optional<Product> existingOpt = productRepository.findByNameIgnoreCase(dto.getName().trim());
        if (existingOpt.isPresent()) {
            Product existing = existingOpt.get();

            // inactive bo'lsa — qayta yoqamiz
            if (!existing.isActive()) {
                existing.setActive(true);
                existing.setAvailableForSale(true);
            } else {
                // active bo'lsa — duplicate yaratmaymiz
                throw new RuntimeException("Bunday mahsulot allaqachon mavjud: " + existing.getName());
            }

            applyDtoToProduct(existing, dto, true);
            return productRepository.save(existing);
        }

        Product product = Product.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .availableForSale(dto.getAvailableForSale() != null ? dto.getAvailableForSale() : true)
                .active(true)
                .soldBy(dto.getSoldBy() != null ? SoldBy.valueOf(dto.getSoldBy()) : SoldBy.EACH)
                .price(dto.getPrice())
                .cost(dto.getCost())
                .sku(dto.getSku())
                .composite(dto.getComposite() != null ? dto.getComposite() : false)
                .trackStock(dto.getTrackStock() != null ? dto.getTrackStock() : false)
                .quantity(dto.getQuantity())
                .lowQuantity(dto.getLowQuantity())
                .build();

        // Category ulash
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Saqlab olamiz
        product = productRepository.save(product);

        // Composite ingredientlar
        if (Boolean.TRUE.equals(product.isComposite()) && dto.getIngredients() != null) {
            attachCompositeIngredients(product, dto.getIngredients());
            // cost auto
            product.setCost(calculateCompositeCost(product));
        }

        // ModifierGroups
        if (dto.getModifierGroupIds() != null) {
            List<Modifier> groups = modifierRepository.findAllById(dto.getModifierGroupIds());
            product.setModifierGroups(groups);
        }

        return productRepository.save(product);
    }

    // ✅ UPDATE
    @Transactional
    public Product update(UUID productId, ProductDto dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        applyDtoToProduct(product, dto, false);
        return productRepository.save(product);
    }

    // 🖼️ Upload image
    @Transactional
    public Product uploadImage(UUID productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getImageUrl() != null) {
            fileStorageService.deleteFile(product.getImageUrl());
        }

        String imageUrl = fileStorageService.storeFile(file);
        product.setImageUrl(imageUrl);

        return productRepository.save(product);
    }

    // 🖼️ Delete image
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

    // ✅ GET ALL (faqat active)
    public List<Product> getAll() {
        return productRepository.findAllByActiveTrue();
    }

    // ✅ GET ONE
    public Product getById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // ✅ SEARCH (faqat active)
    public List<Product> universalSearch(String value) {
        if (value == null || value.isBlank()) {
            return getAll();
        }
        return productRepository.searchActiveByNameOrSku(value.trim());
    }

    // ✅ SOFT DELETE (inactive)
    @Transactional
    public void delete(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // rasmni o‘chirish shart emas (xohlasang qoldirasan), lekin sotuvdan o‘chiriladi
        product.setAvailableForSale(false);
        product.setActive(false);

        productRepository.save(product);
    }

    // ✅ Recalculate composite costs
    @Transactional
    public void recalcCompositeCostsForProducts(List<UUID> productIds) {
        List<Product> products = productRepository.findAllById(productIds);
        for (Product p : products) {
            if (p.isComposite() && p.getIngredients() != null) {
                p.setCost(calculateCompositeCost(p));
                productRepository.save(p);
            }
        }
    }

    // ----------------- Helpers -----------------

    private void applyDtoToProduct(Product product, ProductDto dto, boolean isCreateFlow) {

        if (dto.getName() != null && !dto.getName().isBlank()) {
            // Update paytida nomni o‘zgartirish mumkin, lekin duplicate bo‘lib qolmasin
            String newName = dto.getName().trim();
            Optional<Product> other = productRepository.findByNameIgnoreCase(newName);
            if (other.isPresent() && !other.get().getId().equals(product.getId())) {
                throw new RuntimeException("Bunday nomdagi mahsulot bor: " + newName);
            }
            product.setName(newName);
        }

        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getAvailableForSale() != null) product.setAvailableForSale(dto.getAvailableForSale());
        if (dto.getSoldBy() != null) product.setSoldBy(SoldBy.valueOf(dto.getSoldBy()));
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());

        // SKU (create flowda ham)
        if (dto.getSku() != null && !dto.getSku().isBlank()) {
            product.setSku(dto.getSku());
        } else if (isCreateFlow && (product.getSku() == null || product.getSku().isBlank())) {
            product.setSku(generateSku());
        }

        if (dto.getTrackStock() != null) product.setTrackStock(dto.getTrackStock());
        if (dto.getQuantity() != null) product.setQuantity(dto.getQuantity());
        if (dto.getLowQuantity() != null) product.setLowQuantity(dto.getLowQuantity());

        // Category
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Composite
        if (dto.getComposite() != null) {
            product.setComposite(dto.getComposite());

            // ingredientlarni tozalash
            if (product.getIngredients() != null) product.getIngredients().clear();

            if (Boolean.TRUE.equals(dto.getComposite()) && dto.getIngredients() != null) {
                attachCompositeIngredients(product, dto.getIngredients());
                product.setCost(calculateCompositeCost(product));
            } else {
                // oddiy product cost
                if (dto.getCost() != null) product.setCost(dto.getCost());
            }
        } else {
            // composite kelmagan bo‘lsa, faqat cost update
            if (dto.getCost() != null) product.setCost(dto.getCost());
        }

        // Modifier groups
        if (dto.getModifierGroupIds() != null) {
            if (dto.getModifierGroupIds().isEmpty()) {
                product.getModifierGroups().clear();
            } else {
                List<Modifier> groups = modifierRepository.findAllById(dto.getModifierGroupIds());
                product.setModifierGroups(groups);
            }
        }
    }

    private void attachCompositeIngredients(Product parent, List<CompositeItemDto> items) {
        if (parent.getIngredients() == null) {
            parent.setIngredients(new ArrayList<>());
        }

        for (CompositeItemDto itemDto : items) {
            Product ingredient = productRepository.findById(itemDto.getIngredientProductId())
                    .orElseThrow(() -> new RuntimeException("Ingredient product not found"));

            // ✅ ingredient inactive bo‘lsa — retseptga qo‘shishga ruxsat bermaymiz
            if (!ingredient.isActive()) {
                throw new RuntimeException("Ingredient inactive: " + ingredient.getName());
            }

            CompositeItem compositeItem = CompositeItem.builder()
                    .parentProduct(parent)
                    .ingredientProduct(ingredient)
                    .quantity(itemDto.getQuantity())
                    .build();

            parent.getIngredients().add(compositeItem);
        }
    }

    private double calculateCompositeCost(Product product) {
        if (product.getIngredients() == null) return 0.0;
        return product.getIngredients().stream()
                .mapToDouble(i -> {
                    Double c = i.getIngredientProduct().getCost();
                    Double q = i.getQuantity();
                    return (c == null ? 0.0 : c) * (q == null ? 0.0 : q);
                })
                .sum();
    }

    // SKU AUTO-GENERATE (1001, 1002...)
    public String generateSku() {
        String lastSku = productRepository.findMaxSku();
        if (lastSku == null || lastSku.isBlank()) return "1001";
        try {
            return String.valueOf(Integer.parseInt(lastSku) + 1);
        } catch (Exception e) {
            return "1001";
        }
    }
}
