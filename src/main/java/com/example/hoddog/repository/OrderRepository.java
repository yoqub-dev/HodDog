package com.example.hoddog.repository;

import com.example.hoddog.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT MIN(o.createdAt) FROM Order o")
    Optional<LocalDateTime> findOldestOrderDate();


}
