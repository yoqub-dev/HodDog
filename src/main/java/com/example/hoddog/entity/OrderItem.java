package com.example.hoddog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    @ManyToOne
    private Product product;

    private Double unitCost;
    private Double lineCogs;


    private Double price;      // product price at sale time
    private Integer quantity;
    private Double lineTotal;  // price * quantity + modifiers total

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private List<OrderModifier> modifiers = new ArrayList<>();
}

