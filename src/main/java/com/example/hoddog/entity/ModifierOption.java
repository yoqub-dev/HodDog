package com.example.hoddog.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "modifier_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifierOption {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "modifier_id", nullable = false)
    @JsonBackReference
    private Modifier modifier;  // option → group

    @Column(nullable = false)
    private String name; // e.g. Banana, Olma, Kivi

    private Double price = 0.0;
}
