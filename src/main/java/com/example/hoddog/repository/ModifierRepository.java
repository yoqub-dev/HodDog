package com.example.hoddog.repository;

import com.example.hoddog.entity.Modifier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModifierRepository extends JpaRepository<Modifier, UUID> {
    boolean existsByName(String cheese);
}
