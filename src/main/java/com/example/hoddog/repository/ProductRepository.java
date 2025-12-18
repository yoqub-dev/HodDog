package com.example.hoddog.repository;

import com.example.hoddog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    // =========================
    // SKU
    // =========================
    @Query("select max(p.sku) from Product p")
    String findMaxSku();

    // =========================
    // NAME CHECK (duplicate)
    // =========================
    boolean existsByNameIgnoreCase(String name);

    Optional<Product> findByNameIgnoreCase(String name);

    // =========================
    // ACTIVE FILTERED QUERIES
    // =========================
    List<Product> findAllByActiveTrue();

    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);

    @Query("""
        select p from Product p
        where p.active = true
          and (
            lower(p.name) like lower(concat('%', :value, '%'))
            or lower(p.sku) like lower(concat('%', :value, '%'))
          )
    """)
    List<Product> searchActiveByNameOrSku(String value);

    // =========================
    // INGREDIENT RELATION
    // =========================
    /**
     * Qaysi mahsulotlar shu ingredientni ishlatyapti
     * (ingredient delete qilinishidan OLDIN tekshirish uchun)
     */
    List<Product> findAllByIngredientsIngredientProductId(UUID ingredientProductId);

    // =========================
    // ❗ ESKI METHODLAR (AGAR QAYERDADIR ISHLATILAYOTGAN BO'LSA)
    // =========================
    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
            String name,
            String sku
    );
}
