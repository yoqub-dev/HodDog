package com.example.hoddog.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Table(name = "modifier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modifier {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name; // e.g. Meva

    private boolean active = true;

    @OneToMany(mappedBy = "modifier", cascade = CascadeType.ALL)
    private List<ModifierOption> options = new ArrayList<>();
}
