package com.example.hoddog.service;

import com.example.hoddog.dto.*;
import com.example.hoddog.entity.*;
import com.example.hoddog.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductComponentRepository componentRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {

        validate(request);

        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = productRepository.save(Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .price(request.getPrice())
                .cost(request.getCost())
                .soldBy(request.getSoldBy())
                .trackStock(request.isTrackStock())
                .quantity(request.getQuantity())
                .compositeItem(request.isCompositeItem())
                .category(category)
                .build()
        );

        List<ComponentResponse> componentResponses = null;

        if (request.isCompositeItem() && request.getComponents() != null) {
            var components = request.getComponents().stream()
                    .map(c -> ProductComponent.builder()
                            .parentProduct(product)
                            .childProduct(productRepository.findById(c.getProductId())
                                    .orElseThrow(() -> new RuntimeException("Child product not found")))
                            .quantity(c.getQuantity())
                            .build())
                    .toList();

            componentRepository.saveAll(components);

            componentResponses = components.stream()
                    .map(c -> ComponentResponse.builder()
                            .productId(c.getChildProduct().getId())
                            .productName(c.getChildProduct().getName())
                            .quantity(c.getQuantity())
                            .build()).toList();
        }

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .compositeItem(product.isCompositeItem())
                .categoryName(category.getName())
                .components(componentResponses)
                .build();
    }

    private void validate(ProductCreateRequest r) {


        if (!r.isCompositeItem() && r.getComponents() != null && !r.getComponents().isEmpty()) {
            throw new RuntimeException("This product is not composite. Remove components.");
        }


        if (r.isCompositeItem() && (r.getComponents() == null || r.getComponents().isEmpty())) {
            throw new RuntimeException("Composite product must include at least one component.");
        }
    }

    @Transactional
    public ProductResponse update(UUID id, ProductCreateRequest request) {

        validate(request);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        var category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setPrice(request.getPrice());
        product.setCost(request.getCost());
        product.setSoldBy(request.getSoldBy());
        product.setTrackStock(request.isTrackStock());
        product.setQuantity(request.getQuantity());
        product.setCompositeItem(request.isCompositeItem());
        product.setCategory(category);

        componentRepository.deleteAll(product.getComponents());
        product.setComponents(null);

        List<ComponentResponse> componentResponses = null;

        if (request.isCompositeItem()) {
            var newComponents = request.getComponents().stream()
                    .map(c -> ProductComponent.builder()
                            .parentProduct(product)
                            .childProduct(productRepository.findById(c.getProductId())
                                    .orElseThrow(() -> new RuntimeException("Child product not found")))
                            .quantity(c.getQuantity())
                            .build())
                    .toList();

            componentRepository.saveAll(newComponents);
            product.setComponents(newComponents);

            componentResponses = newComponents.stream()
                    .map(c -> ComponentResponse.builder()
                            .productId(c.getChildProduct().getId())
                            .productName(c.getChildProduct().getName())
                            .quantity(c.getQuantity())
                            .build()).toList();
        }

        return toResponse(product, componentResponses);
    }

    public ProductResponse get(UUID id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ComponentResponse> components = null;

        if (product.isCompositeItem()) {
            components = product.getComponents().stream()
                    .map(c -> ComponentResponse.builder()
                            .productId(c.getChildProduct().getId())
                            .productName(c.getChildProduct().getName())
                            .quantity(c.getQuantity())
                            .build())
                    .toList();
        }

        return toResponse(product, components);
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(p -> get(p.getId()))
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        componentRepository.deleteAll(product.getComponents());
        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product, List<ComponentResponse> components) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .categoryName(product.getCategory().getName())
                .compositeItem(product.isCompositeItem())
                .components(components)
                .build();
    }

}
