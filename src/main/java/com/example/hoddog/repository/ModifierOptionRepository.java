package com.example.hoddog.repository;

import com.example.hoddog.entity.ModifierOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ModifierOptionRepository extends JpaRepository<ModifierOption, UUID> {
}
