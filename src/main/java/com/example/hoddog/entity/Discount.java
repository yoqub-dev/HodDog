package com.example.hoddog.entity;

import com.example.hoddog.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private DiscountType type; // PERCENTAGE or AMOUNT

    // value:
    // if type = PERCENTAGE → 5, 10, 25 etc
    // if type = AMOUNT → 2000 so'm
    private Double value;
}
