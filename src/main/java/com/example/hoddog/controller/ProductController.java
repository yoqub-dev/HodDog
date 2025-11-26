package com.example.hoddog.controller;

import com.example.hoddog.dto.ProductDto;
import com.example.hoddog.entity.Product;
import com.example.hoddog.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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


    @PostMapping("/{id}/upload-image")
    public ResponseEntity<Product> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {

        Product product = productService.uploadImage(id, file);
        return ResponseEntity.ok(product);
    }

    // 🖼️ YANGI: Rasmni o'chirish
    @DeleteMapping("/{id}/image")
    public ResponseEntity<Product> deleteImage(@PathVariable UUID id) {
        Product product = productService.deleteImage(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getProductImage(@PathVariable UUID id) {
        Product product = productService.getById(id);

        if (product.getImageUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String fileName = product.getImageUrl()
                    .substring(product.getImageUrl().lastIndexOf("/") + 1);

            Path filePath = Paths.get("uploads/products").resolve(fileName);

            byte[] imageBytes = Files.readAllBytes(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // yoki PNG
                    .body(imageBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    public List<Product> search(@RequestParam String value) {
        return productService.universalSearch(value);
    }

}