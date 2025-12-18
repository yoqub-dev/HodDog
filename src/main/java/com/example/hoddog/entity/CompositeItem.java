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

    @ManyToOne
    @JoinColumn(name = "parent_product_id", nullable = false)
    @JsonIgnore
    private Product parentProduct;

    @ManyToOne
    @JoinColumn(name = "ingredient_product_id", nullable = false)
    private Product ingredientProduct;

    @Column(nullable = false)
    private Double quantity;
}
