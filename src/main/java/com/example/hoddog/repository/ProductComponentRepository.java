package com.example.hoddog.repository;

import com.example.hoddog.entity.ProductComponent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductComponentRepository extends JpaRepository<ProductComponent, UUID> {
}
