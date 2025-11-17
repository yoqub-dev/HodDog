package com.example.hoddog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "composite_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompositeItem {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Parent product – asosiy mahsulot
     * Masalan: Hotdog
     */
    @ManyToOne
    @JoinColumn(name = "parent_product_id", nullable = false)
    @JsonIgnore
    private Product parentProduct;

    /**
     * Ingredient – ichiga qo‘shiladigan mahsulot
     * Masalan: Sosiska, Non, Sous
     */
    @ManyToOne
    @JoinColumn(name = "ingredient_product_id", nullable = false)
    private Product ingredientProduct;

    /**
     * Ingredient miqdori
     * Misol: 1 ta sosiska, 1 ta non, 2 qoshiq sous
     */
    @Column(nullable = false)
    private int quantity;
}
