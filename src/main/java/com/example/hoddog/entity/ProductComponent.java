package com.example.hoddog.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "parent_product_id")
    private Product parentProduct;

    @ManyToOne
    @JoinColumn(name = "child_product_id")
    private Product childProduct;

    private Double quantity;
}
