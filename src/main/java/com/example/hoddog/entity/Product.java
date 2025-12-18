package com.example.hoddog.entity;

import com.example.hoddog.enums.SoldBy;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_products_name", columnNames = {"name"}),
                @UniqueConstraint(name = "uk_products_sku", columnNames = {"sku"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 150)
    private String name;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;              // ✅ SOFT DELETE FLAG

    @Column(nullable = false)
    private boolean availableForSale = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SoldBy soldBy = SoldBy.EACH;

    private Double price;

    private Double cost;

    @Column(length = 50, unique = true)
    private String sku;

    @Column(nullable = false)
    private boolean composite = false;

    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<CompositeItem> ingredients = new ArrayList<>();

    @Column(nullable = false)
    private boolean trackStock = false;

    private Double quantity;

    private Double lowQuantity;

    @Column(nullable = false)
    private boolean lowStockNotified = false;

    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "product_modifiers",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "modifier_id")
    )
    @Builder.Default
    private List<Modifier> modifierGroups = new ArrayList<>();
}
