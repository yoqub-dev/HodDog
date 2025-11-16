package com.example.hoddog.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {

    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Rasmning saqlangan joyi (URL yoki local storage path)
     * Misollar:
     *  /uploads/products/hdg-001.png
     */
    @Column(nullable = false)
    private String imageUrl;

    /**
     * Asosiy mahsulotga bog'lanish
     */
    @OneToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
