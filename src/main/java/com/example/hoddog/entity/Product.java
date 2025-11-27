package com.example.hoddog.entity;

import com.example.hoddog.enums.SoldBy;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;


    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;


    @Column(length = 500)
    private String description;

    private boolean availableForSale = true;

    @Enumerated(EnumType.STRING)
    private SoldBy soldBy = SoldBy.EACH;

    private Double price;

    private Double cost;

    @Column(unique = true)
    private String sku;

    private boolean composite = false;

    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonManagedReference
    private List<CompositeItem> ingredients = new ArrayList<>();

    private boolean trackStock = false;

    private Double quantity;

    private Double lowQuantity;

    private boolean lowStockNotified = false;

    private String imageUrl;

    @ManyToMany
    @JoinTable(
            name = "product_modifiers",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "modifier_id")
    )
    private List<Modifier> modifierGroups = new ArrayList<>();
}
