package com.example.hoddog.entity;

import com.example.hoddog.enums.SoldBy;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;
    private String description;
    private String sku;
    private String barcode;
    private Double price;
    private Double cost;

    @Enumerated(EnumType.STRING)
    private SoldBy soldBy;

    private boolean compositeItem;
    private boolean trackStock;
    private Double quantity;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    // ✅ Combo tarkibi
    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL)
    private List<ProductComponent> components;
}
